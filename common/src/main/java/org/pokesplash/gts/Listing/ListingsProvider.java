package org.pokesplash.gts.Listing;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.pokesplash.gts.Gts;
import org.pokesplash.gts.api.provider.ListingAPI;
import org.pokesplash.gts.util.Deserializer;
import org.pokesplash.gts.util.Utils;

import java.io.File;
import java.util.*;

/**
 * Manages all types of listings. Data is saved to memory here.
 */
public class ListingsProvider {
	protected ArrayList<Listing> listings; // Active listings.

	protected HashMap<UUID, ArrayList<Listing>> expiredListings; // Expired listings.

	/**
	 * Constructor to create a new list for both hashmaps.
	 */
	public ListingsProvider() {
		listings = new ArrayList<>();
		expiredListings = new HashMap<>();
	}

	/**
	 * Relists all expired listings of a player.
	 * @param player The player that the expired listings should be relisted.
	 */
	public void relistAllExpiredListings(UUID player) {
		ArrayList<Listing> expired = expiredListings.get(player);

		if (expired == null) {
			return;
		}

		if (ListingAPI.getHighestPriority() != null) {

			List<Listing> clonedList = expired.stream().map(Listing::deepClone).toList();

			for (Listing listing : clonedList) {
				listing.renewEndTime();
				ListingAPI.getHighestPriority().update(listing);
			}
			return;
		}

		for (Listing listing : expired) {
			listing.renewEndTime();
			addListing(listing);
		}

		expiredListings.remove(player);
	}

	/**
	 * Method that returns all active listings.
	 * @return A list of active listings.
	 */
	public List<Listing> getListings() {
		return listings;
	}


	/**
	 * Method to get all pokemon listings, as a collection.
	 * @return Pokemon listings in a collection.
	 */
	public List<PokemonListing> getPokemonListings() {

		ArrayList<PokemonListing> pkmListings = new ArrayList<>();

		// Filters all listings and finds the listings that are Pokemon only.
		for (Listing listing : listings) {
			if (listing instanceof PokemonListing) {
				pkmListings.add((PokemonListing) listing);
			}
		}

		return pkmListings;
	}

	/**
	 * Method to get all listings that have been listed by a specific UUID.
	 * @param uuid the uuid to get the listings for.
	 * @return Arraylist of listings from the specified player.
	 */
	public List<Listing> getListingsByPlayer(UUID uuid) {
		ArrayList<Listing> playerListings = new ArrayList<>();

		for (Listing listing : getListings()) {
			if (listing.getSellerUuid().equals(uuid)) {
				playerListings.add(listing);
			}
		}
		return playerListings;
	}

	/**
	 * Method to get all pokemon listings that have been listed by a specific UUID.
	 * @param uuid the uuid to get the pokemon listings for.
	 * @return Arraylist of pokemon listings from the specified player.
	 */
	public List<PokemonListing> getPokemonListingsByPlayer(UUID uuid) {
		ArrayList<PokemonListing> playerListings = new ArrayList<>();

		for (PokemonListing pokemonListing : getPokemonListings()) {
			if (pokemonListing.getSellerUuid().equals(uuid)) {
				playerListings.add(pokemonListing);
			}
		}
		return playerListings;
	}

	/**
	 * Method to get all item listings, as a collection.
	 * @return item listings in a collection.
	 */
	public List<ItemListing> getItemListings() {
		ArrayList<ItemListing> itemListings = new ArrayList<>();

		// Filters all listings and finds the listings that are Pokemon only.
		for (Listing listing : listings) {
			if (listing instanceof ItemListing) {
				itemListings.add((ItemListing) listing);
			}
		}

		return itemListings;
	}

	/**
	 * Method to get all item listings that have been listed by a specific UUID.
	 * @param uuid the uuid to get the item listings for.
	 * @return Arraylist of item listings from the specified player.
	 */
	public List<ItemListing> getItemListingsByPlayer(UUID uuid) {
		ArrayList<ItemListing> playerListings = new ArrayList<>();

		for (ItemListing item : getItemListings()) {
			if (item.getSellerUuid().equals(uuid)) {
				playerListings.add(item);
			}
		}
		return playerListings;
	}

	/**
	 * Method to add a new listing to the array.
	 * @param listing the listing to add.
	 * @return true if the listing was successfully added.
	 * @throws IllegalArgumentException If the listing already exists.
	 */
	public boolean addListing(Listing listing) throws IllegalArgumentException {

		if (hasListing(listing.getId(), listings)) {
			throw new IllegalArgumentException("This listing already exists!");
		}
		listings.add(listing);
		return listing.write(Gts.LISTING_FILE_PATH);
	}

	/**
	 * Method to remove a listing (expired or bought)
	 * @param listing The listing that should be removed.
	 * @return true if the listing was successfully removed.
	 * @throws IllegalArgumentException if the listing doesn't exist.
	 */
	public boolean removeListing(Listing listing) throws IllegalArgumentException {
		if (!hasListing(listing.getId(), listings)) {
			throw new IllegalArgumentException("No listing with the UUID " + listing.getId() + " exists.");
		}

		return listings.remove(listing);
	}

	/**
	 * Method to check if a pokemon listing already exists.
	 * @param id the listing to check for
	 * @param listings the list to check in
	 * @return true if the listing already exists in the list.
	 */
	private boolean hasListing(UUID id, List<Listing> listings) {
		for (Listing listing : listings) {
			if (listing.getId().equals(id)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks that a player has some expired listings.
	 * @param playerUUID The player to check for expired listings
	 * @return true if the player has expired listings.
	 */
	public boolean hasExpiredListings(UUID playerUUID) {
		return expiredListings.containsKey(playerUUID);
	}

	/**
	 * Method to add an expired listing to the list.
	 * @param listing The listing to add.
	 * @return true if successfully written to file.
	 */
	public boolean addExpiredListing(Listing listing) {
		if (expiredListings.containsKey(listing.getSellerUuid())) {
			ArrayList<Listing> currentListings = expiredListings.get(listing.getSellerUuid());
			if (!currentListings.contains(listing)) {
				currentListings.add(listing);
				expiredListings.put(listing.getSellerUuid(), currentListings);
			}
		} else {
			expiredListings.put(listing.getSellerUuid(), new ArrayList<>(List.of(listing)));
		}
		return true;
	}

	/**
	 * Method to remove an expired listing that has been collected by the player.
	 * @param listing The listing to remove.
	 * @return true if successfully written to file.
	 */
	public boolean removeExpiredListing(Listing listing) {

		if (expiredListings.get(listing.getSellerUuid()) == null) {
			return false;
		}

		ArrayList<Listing> listings = expiredListings.get(listing.getSellerUuid());
		if (listings.contains(listing)) {
			listings.remove(listing);
			expiredListings.put(listing.getSellerUuid(), listings);
			return true;
		} else {
			return false;
		}
	}

	public Listing getActiveListingById(UUID id) {
		for (Listing listing : listings) {
			if (listing.getId().equals(id)) {
				return listing;
			}
		}

		return null;
	}

	public Listing getExpiredListingById(UUID id) {
		for (ArrayList<Listing> playerListings : expiredListings.values()) {
			for (Listing listing : playerListings) {
				if (listing.getId().equals(id)) {
					return listing;
				}
			}
		}

		return null;
	}

	public Listing getListingById(UUID id) {
		Listing activeListing = getActiveListingById(id);

		if (activeListing != null) {
			return activeListing;
		}

        return getExpiredListingById(id);
    }


	public List<Listing> getExpiredListingsOfPlayer(UUID player) {

		if (expiredListings.get(player) == null) {
			return Collections.emptyList();
		}

		return expiredListings.get(player);
	}

	public HashMap<UUID, ArrayList<Listing>> getExpiredListings() {
		return expiredListings;
	}

	public void check() {
		ArrayList<Listing> toRemove = new ArrayList<>();

		for (Listing listing : listings) {
			if (listing.getEndTime() < new Date().getTime() &&
					listing.getEndTime() != -1) {
				toRemove.add(listing);
			}
		}

		for (Listing listing : toRemove) {
			boolean success = removeListing(listing);
			addExpiredListing(listing);
		}
	}


	/**
	 * Method to load the listings from file.
	 */
	public void init() {

		listings = new ArrayList<>();

		File dir = Utils.checkForDirectory(Gts.LISTING_FILE_PATH);

		String[] list = dir.list();

        if (list.length != 0) {
			for (String file : list) {
				Utils.readFileAsync(Gts.LISTING_FILE_PATH, file, el -> {
					GsonBuilder builder = new GsonBuilder();
					// Type adapters help gson deserialize the listings interface.
					builder.registerTypeAdapter(Listing.class, new Deserializer(PokemonListing.class));
					builder.registerTypeAdapter(Listing.class, new Deserializer(ItemListing.class));
					Gson gson = builder.create();

					Listing listing = gson.fromJson(el, Listing.class);

					listing = listing.isPokemon() ? gson.fromJson(el, PokemonListing.class) :
							gson.fromJson(el, ItemListing.class);

					if (!listing.getVersion().equals(Gts.LISTING_FILE_VERSION)) {
						// TODO upgrade listing file (Future use).
					}

					if (listing.getEndTime() > new Date().getTime() ||
						listing.getEndTime() == -1) {
						listings.add(listing);
					} else {
						addExpiredListing(listing);
					}
				});
			}
		}
	}
}
