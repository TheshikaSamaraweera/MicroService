package com.democode.order_service.service;

import com.democode.order_service.dto.InventoryResponse;
import com.democode.order_service.dto.OrderLineItemsDto;
import com.democode.order_service.dto.OrderRequest;
import com.democode.order_service.model.Order;
import com.democode.order_service.model.OrderLineItems;
import com.democode.order_service.reopsitory.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient webClient;


    public void placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .toList();

        order.setOrderLineItemsList(orderLineItems);

        List<String> skuCodes = order.getOrderLineItemsList().stream()
                .map(OrderLineItems::getSkuCode)
                .toList();

        //call the inventory service and place order if product is in stock

     InventoryResponse[] inventoryResponsesArray =   webClient.get() // call to service
                        .uri("http://localhost:8082/api/inventory" ,
                                uriBuilder ->uriBuilder.queryParam("skuCode" ,skuCodes).build())
                                .retrieve() // retrive from request
                                        .bodyToMono(InventoryResponse[].class)
                                                .block(); //to make synchonous call

        boolean allProductInStock = Arrays.stream(inventoryResponsesArray)
                .allMatch(InventoryResponse::isInStock);

        if(allProductInStock){
            orderRepository.save(order);
        }else {
            throw new IllegalArgumentException("product is not in stock please try agian later");
        }



    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItems;
    }
}