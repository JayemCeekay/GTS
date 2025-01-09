package org.pokesplash.gts.history;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.world.item.ItemStack;
import org.pokesplash.gts.Gts;
import org.pokesplash.gts.Listing.ItemListing;

/**
 * Class that is used to save sold Items.
 */
public class ItemHistoryItem extends HistoryItem<ItemStack> {

    private JsonElement item;

    public ItemHistoryItem(ItemListing listing, String buyerName) {
        super(listing.isPokemon(), listing.getSellerUuid(), listing.getSellerName(), listing.getPrice(), buyerName);
        this.item = ItemStack.CODEC.encodeStart(
                Gts.server.registryAccess().createSerializationContext(JsonOps.INSTANCE),
                listing.getListing()).getOrThrow();
    }

    /**
     * Method to get the Item that has been sold.
     * @return The Item that has been sold.
     */
    @Override
    public ItemStack getListing() {
        try {
            ItemStack itemStack = ItemStack.CODEC.decode(Gts.server.registryAccess().createSerializationContext(JsonOps.INSTANCE),
                    item).getOrThrow().getFirst();
            return itemStack;
        } catch (IllegalStateException e) {
            Gts.LOGGER.fatal("Failed to parse item for " + item);
            Gts.LOGGER.fatal("Stacktrace: ");
            e.printStackTrace();
        }
        return null;
    }
}
