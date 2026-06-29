package shopingmall.myshop.inventory.dto;

import shopingmall.myshop.inventory.domain.Inventory;

public record InventoryResponse(
        Long productId,
        int totalQuantity,
        int reservedQuantity,
        int availableQuantity
) {
    public static InventoryResponse from(Inventory inventory) {
        return new InventoryResponse(
                inventory.getProductId(),
                inventory.getTotalQuantity(),
                inventory.getReservedQuantity(),
                inventory.getAvailableQuantity()
        );
    }
}
