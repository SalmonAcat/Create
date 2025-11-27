import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

@@ -16,7 +19,6 @@
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour.TankSegment;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.recipe.DummyCraftingContainer;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;

import net.createmod.catnip.data.Iterate;
@@ -147,8 +149,27 @@ private static boolean apply(BasinBlockEntity basin, Recipe<?> recipe, boolean t
			}

			if (simulate) {
				CraftingInput remainderInput = new DummyCraftingContainer(availableItems, extractedItemsFromSlot)
					.asCraftInput();
				CraftingInput remainderInput = CraftingInput.of(3, 3,
					IntStream.range(0, availableItems.getSlots())
						.boxed()
						.flatMap(slot -> {
							var used = extractedItemsFromSlot[slot];
							if (used > 0) {
								ItemStack stack = availableItems.getStackInSlot(slot).copy();
								stack.setCount(1);
								return IntStream.range(0, used).mapToObj(i -> stack.copy());
							}
							return Stream.empty();
						})
						.collect(Collectors.collectingAndThen(
							Collectors.toCollection(ArrayList::new),
							list -> {
								while (list.size() < 9)
									list.add(ItemStack.EMPTY);
								return list;
							})
						)
				);

				if (recipe instanceof BasinRecipe basinRecipe) {
					recipeOutputItems.addAll(basinRecipe.rollResults());
@@ -159,7 +180,6 @@ private static boolean apply(BasinBlockEntity basin, Recipe<?> recipe, boolean t
					for (ItemStack stack : basinRecipe.getRemainingItems(remainderInput))
						if (!stack.isEmpty())
							recipeOutputItems.add(stack);

				} else {
					recipeOutputItems.add(recipe.getResultItem(basin.getLevel()
						.registryAccess()));
					if (recipe instanceof CraftingRecipe craftingRecipe) {
						for (ItemStack stack : craftingRecipe.getRemainingItems(remainderInput))
							if (!stack.isEmpty())
								recipeOutputItems.add(stack);
					}
				}
			}
			if (!basin.acceptOutputs(recipeOutputItems, recipeOutputFluids, simulate))
				return false;
		}
		return true;
	}
	public static RecipeHolder<BasinRecipe> convertShapeless(RecipeHolder<?> recipe) {
		BasinRecipe basinRecipe =
			new ProcessingRecipeBuilder<>(BasinRecipe::new, recipe.id()).withItemIngredients(recipe.value().getIngredients())
				.withSingleItemOutput(recipe.value().getResultItem(Minecraft.getInstance().level.registryAccess()))
				.build();
		return new RecipeHolder<>(recipe.id(), basinRecipe);
	}
	protected BasinRecipe(IRecipeTypeInfo type, ProcessingRecipeParams params) {
		super(type, params);
	}
	public BasinRecipe(ProcessingRecipeParams params) {
		this(AllRecipeTypes.BASIN, params);
	}
	@Override
	protected int getMaxInputCount() {
		return 9;
	}
	@Override
	protected int getMaxOutputCount() {
		return 4;
	}
	@Override
	protected int getMaxFluidInputCount() {
		return 2;
	}
	@Override
	protected int getMaxFluidOutputCount() {
		return 2;
	}
	@Override
	protected boolean canRequireHeat() {
		return true;
	}
	@Override
	protected boolean canSpecifyDuration() {
		return true;
	}
	@Override
	public boolean matches(RecipeInput input, @Nonnull Level worldIn) {
		return false;
	}
}
