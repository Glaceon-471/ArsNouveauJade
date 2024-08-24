package ArsNouveauJade;

import com.hollingsworth.arsnouveau.api.ArsNouveauAPI;
import com.hollingsworth.arsnouveau.api.registry.ImbuementRecipeRegistry;
import com.hollingsworth.arsnouveau.api.source.ISourceTile;
import com.hollingsworth.arsnouveau.common.block.tile.EnchantingApparatusTile;
import com.hollingsworth.arsnouveau.common.block.tile.ImbuementTile;
import com.hollingsworth.arsnouveau.common.crafting.recipes.*;
import com.hollingsworth.arsnouveau.setup.registry.BlockRegistry;
import com.hollingsworth.arsnouveau.setup.registry.ItemsRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import snownee.jade.JadeInternals;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public enum JadeComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    // ソースリンク
    AgronomicSourcelink(BlockRegistry.AGRONOMIC_SOURCELINK.registryObject::getId),
    AlchemicalSourcelink(BlockRegistry.ALCHEMICAL_BLOCK.registryObject::getId),
    MycelialSourcelink(BlockRegistry.MYCELIAL_BLOCK.registryObject::getId),
    VitalicSourcelink(BlockRegistry.VITALIC_BLOCK.registryObject::getId),
    VolcanicSourcelink(BlockRegistry.VOLCANIC_BLOCK.registryObject::getId),

    // ソース瓶
    SourceJar(BlockRegistry.SOURCE_JAR.registryObject::getId),

    // 機械系
    EnchantingApparatus((tooltip, accessor, config) -> {
        EnchantingApparatusTile ea = (EnchantingApparatusTile)accessor.getBlockEntity();
        IElementHelper elements = JadeInternals.getElementHelper();
        Level level = accessor.getLevel();
        List<ItemStack> pedestal = ea.getPedestalItems();
        if (!pedestal.isEmpty()) {
            tooltip.add(elements.item(pedestal.getFirst(), 0.5f).size(new Vec2(10, 10)).translate(new Vec2(0, -1)));
            for (int i = 1; i < pedestal.size(); i++) {
                tooltip.append(elements.item(pedestal.get(i), 0.5f).size(new Vec2(10, 10)).translate(new Vec2(0, -1)));
            }
        }

        if (!ea.getStack().isEmpty()) {
            ItemStack item = ea.getStack();
            tooltip.add(elements.item(item, 0.5f).size(new Vec2(10, 10)).translate(new Vec2(0, -1)));
            tooltip.append(item.getHoverName());
            item.getTagEnchantments().addToTooltip(Item.TooltipContext.of(level), tooltip::add, TooltipFlag.NORMAL);
        }

        List<RecipeHolder<? extends IEnchantingRecipe>> holders = ArsNouveauAPI.getInstance().getEnchantingApparatusRecipes(level).stream().filter((r) -> {
            if (r.value() instanceof EnchantingApparatusRecipe recipe) {
                List<Ingredient> ingredients = recipe.pedestalItems();
                return ingredients.size() == pedestal.size() && EnchantingApparatusRecipe.doItemsMatch(pedestal, ingredients);
            }
            return false;
        }).toList();
        RecipeHolder<? extends IEnchantingRecipe> holder = !holders.isEmpty() ? holders.get((accessor.getPlayer().tickCount % (holders.size() * 20)) / 20) : null;

        if (holder != null && holder.value() instanceof EnchantingApparatusRecipe recipe) {
            if (recipe instanceof EnchantmentRecipe er) {
                var enchantment = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(er.enchantmentKey);
                ItemStack reagent = new ItemStack(er.enchantLevel == 1 ? Items.BOOK : Items.ENCHANTED_BOOK);
                ItemStack result = new ItemStack(Items.ENCHANTED_BOOK);
                tooltip.add(Component.translatable("jade.ars_nouveau_jade.candidate"));
                tooltip.add(elements.item(reagent, 0.5f).size(new Vec2(10, 10)).translate(new Vec2(0, -1)));
                tooltip.append(reagent.getHoverName());
                if (reagent.is(Items.ENCHANTED_BOOK)) tooltip.add(Enchantment.getFullname(enchantment, er.enchantLevel - 1));
                tooltip.add(Component.literal("↓"));
                tooltip.append(elements.item(new ItemStack(ItemsRegistry.SOURCE_GEM), 0.5f).size(new Vec2(10, 10)).translate(new Vec2(0, -1)));
                tooltip.append(Component.translatable("jade.ars_nouveau_jade.source_cost", recipe.sourceCost()));
                tooltip.add(elements.item(result, 0.5f).size(new Vec2(10, 10)).translate(new Vec2(0, -1)));
                tooltip.append(result.getHoverName());
                tooltip.add(Enchantment.getFullname(enchantment, er.enchantLevel));
            }
            else if (recipe instanceof ArmorUpgradeRecipe aur) {
                tooltip.add(Component.translatable("jade.ars_nouveau_jade.upgrade", aur.tier, aur.tier + 1));
            }
            else {
                ItemStack reagent = Arrays.stream(recipe.reagent().getItems()).findFirst().orElse(ItemStack.EMPTY);
                ItemStack result = recipe.result();
                if (reagent.isEmpty() || result.isEmpty()) {
                    return;
                }
                tooltip.add(Component.translatable("jade.ars_nouveau_jade.candidate"));
                tooltip.add(elements.item(reagent, 0.5f).size(new Vec2(10, 10)).translate(new Vec2(0, -1)));
                tooltip.append(reagent.getHoverName());
                tooltip.add(Component.literal("↓"));
                tooltip.append(elements.item(new ItemStack(ItemsRegistry.SOURCE_GEM), 0.5f).size(new Vec2(10, 10)).translate(new Vec2(0, -1)));
                tooltip.append(Component.translatable("jade.ars_nouveau_jade.source_cost", recipe.sourceCost()));
                tooltip.add(elements.item(result, 0.5f).size(new Vec2(10, 10)).translate(new Vec2(0, -1)));
                tooltip.append(result.getHoverName());
            }

        }
    }, BlockRegistry.ENCHANTING_APP_BLOCK.registryObject::getId, (tag, accessor) -> { }),

    Imbuement((tooltip, accessor, config) -> {
        ImbuementTile ea = (ImbuementTile)accessor.getBlockEntity();
        IElementHelper elements = JadeInternals.getElementHelper();
        CompoundTag tag = accessor.getServerData();
        tooltip.add(elements.item(new ItemStack(ItemsRegistry.SOURCE_GEM), 0.5f).size(new Vec2(10, 10)).translate(new Vec2(0, -1)));
        tooltip.append(Component.translatable("jade.ars_nouveau_jade.source_amount", tag.getInt("Source"), ea.getMaxSource()));

        List<ItemStack> pedestal = ea.getPedestalItems();
        if (!pedestal.isEmpty()) {
            tooltip.add(elements.item(pedestal.getFirst(), 0.5f).size(new Vec2(10, 10)).translate(new Vec2(0, -1)));
            for (int i = 1; i < pedestal.size(); i++) {
                tooltip.append(elements.item(pedestal.get(i), 0.5f).size(new Vec2(10, 10)).translate(new Vec2(0, -1)));
            }
        }

        List<RecipeHolder<? extends ImbuementRecipe>> holders = ImbuementRecipeRegistry.INSTANCE.getRecipes().stream().filter((r) -> {
            if (r.value() instanceof ImbuementRecipe recipe) {
                List<Ingredient> ingredients = recipe.getPedestalItems();
                return ingredients.size() == pedestal.size() && EnchantingApparatusRecipe.doItemsMatch(pedestal, ingredients);
            }
            return false;
        }).toList();
        RecipeHolder<? extends ImbuementRecipe> holder = !holders.isEmpty() ? holders.get((accessor.getPlayer().tickCount % (holders.size() * 20)) / 20) : null;
        if (holder != null && holder.value() instanceof ImbuementRecipe recipe) {
            tooltip.add(Component.translatable("jade.ars_nouveau_jade.candidate"));
            ItemStack reagent = recipe.getInput().getItems()[0];
            ItemStack result = recipe.getOutput();
            tooltip.add(elements.item(reagent, 0.5f).size(new Vec2(10, 10)).translate(new Vec2(0, -1)));
            tooltip.append(reagent.getHoverName());
            tooltip.add(Component.literal("↓"));
            tooltip.append(elements.item(new ItemStack(ItemsRegistry.SOURCE_GEM), 0.5f).size(new Vec2(10, 10)).translate(new Vec2(0, -1)));
            tooltip.append(Component.translatable("jade.ars_nouveau_jade.source_cost", recipe.getSource()));
            tooltip.add(elements.item(result, 0.5f).size(new Vec2(10, 10)).translate(new Vec2(0, -1)));
            tooltip.append(result.getHoverName());
        }
    }, BlockRegistry.IMBUEMENT_BLOCK.registryObject::getId, (tag, accessor) -> {
        ImbuementTile source = (ImbuementTile)accessor.getBlockEntity();
        tag.putInt("Source", source.getSource());
    });

    private final AppendToolTip AppendFunc;
    private final Supplier<ResourceLocation> GetUidFunc;
    private final BiConsumer<CompoundTag, BlockAccessor> ServerDataFunc;

    JadeComponentProvider(Supplier<ResourceLocation> b) {
        AppendFunc = (tooltip, accessor, config) -> {
            ISourceTile source = (ISourceTile)accessor.getBlockEntity();
            CompoundTag tag = accessor.getServerData();
            IElementHelper elements = JadeInternals.getElementHelper();
            tooltip.add(elements.item(new ItemStack(ItemsRegistry.SOURCE_GEM), 0.5f).size(new Vec2(10, 10)).translate(new Vec2(0, -1)));
            tooltip.append(Component.translatable("jade.ars_nouveau_jade.source_amount", tag.getInt("Source"), source.getMaxSource()));
        };
        GetUidFunc = b;
        ServerDataFunc = (tag, accessor) -> {
            ISourceTile source = (ISourceTile)accessor.getBlockEntity();
            tag.putInt("Source", source.getSource());
        };
    }

    JadeComponentProvider(AppendToolTip a, Supplier<ResourceLocation> b, BiConsumer<CompoundTag, BlockAccessor> c) {
        AppendFunc = a;
        GetUidFunc = b;
        ServerDataFunc = c;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        AppendFunc.accept(tooltip, accessor, config);
    }

    @Override
    public ResourceLocation getUid() {
        return GetUidFunc.get();
    }

    @Override
    public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
        ServerDataFunc.accept(tag, accessor);
    }

    @FunctionalInterface
    public interface AppendToolTip {
        void accept(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config);
    }
}
