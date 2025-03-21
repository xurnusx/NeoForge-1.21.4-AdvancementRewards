package net.urnus.advancementrewardsmod;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.neoforge.common.util.Lazy;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AdvancementRewardsConfig {
    private static final Map<ResourceLocation, ItemStack[]> rewards = new HashMap<>();

    public static void loadConfig() {
        File configFile = new File("config/advancement_rewards.json");
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                JsonElement jsonElement = JsonParser.parseReader(reader);
                JsonObject jsonObject = jsonElement.getAsJsonObject();

                for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                    ResourceLocation advancement = ResourceLocation.parse(entry.getKey());
                    JsonElement rewardElement = entry.getValue();

                    if (rewardElement.isJsonObject()) {
                        // Single reward
                        ItemStack stack = parseReward(rewardElement.getAsJsonObject());
                        if (stack != null) {
                            rewards.put(advancement, new ItemStack[]{stack});
                        }
                    } else if (rewardElement.isJsonArray()) {
                        // Multiple rewards
                        JsonArray rewardsArray = rewardElement.getAsJsonArray();
                        ItemStack[] stacks = new ItemStack[rewardsArray.size()];
                        for (int i = 0; i < rewardsArray.size(); i++) {
                            stacks[i] = parseReward(rewardsArray.get(i).getAsJsonObject());
                        }
                        rewards.put(advancement, stacks);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static ItemStack parseReward(JsonObject rewardObject) {
        // Get the item from the registry (as an Optional<Holder.Reference<Item>>)
        Optional<Holder.Reference<Item>> itemHolder = BuiltInRegistries.ITEM.get(
                ResourceLocation.parse(rewardObject.get("item").getAsString())
        );

        int count = rewardObject.has("amount") ? rewardObject.get("amount").getAsInt() : 1;

        // Check if the item exists and is valid
        if (itemHolder.isPresent()) {
            Holder.Reference<Item> holder = itemHolder.get();
            Item item = holder.value(); // Extract the Item from the Holder
            return new ItemStack(item, count);
        }
        return null;
    }

    public static ItemStack[] getReward(ResourceLocation advancement) {
        return rewards.getOrDefault(advancement, new ItemStack[0]);
    }
}