package shopingmall.myshop.delivery.dto;

import shopingmall.myshop.delivery.domain.Delivery;
import shopingmall.myshop.delivery.domain.DeliveryStatus;

public record DeliveryResponse(
        Long deliveryId,
        DeliveryStatus deliveryStatus,
        String receiverName,
        String receiverPhone,
        String zipcode,
        String address1,
        String address2
) {
    public static DeliveryResponse from(Delivery delivery) {
        return new DeliveryResponse(
                delivery.getId(),
                delivery.getDeliveryStatus(),
                delivery.getReceiverName(),
                delivery.getReceiverPhone(),
                delivery.getZipcode(),
                delivery.getAddress1(),
                delivery.getAddress2()
        );
    }
}
