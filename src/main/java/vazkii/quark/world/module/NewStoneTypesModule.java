package vazkii.quark.world.module;

import java.util.ArrayDeque;
import java.util.Queue;

import com.google.common.base.Supplier;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.ItemGroup;
import net.minecraft.world.gen.GenerationStage.Decoration;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.handler.VariantHandler;
import vazkii.quark.base.module.Config;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.Module;
import vazkii.quark.base.module.ModuleCategory;
import vazkii.quark.base.world.WorldGenHandler;
import vazkii.quark.base.world.WorldGenWeights;
import vazkii.quark.base.world.generator.OreGenerator;
import vazkii.quark.world.config.StoneTypeConfig;

@LoadModule(category = ModuleCategory.WORLD)
public class NewStoneTypesModule extends Module {

	@Config(flag = "marble") public static boolean enableMarble = true;
	@Config(flag = "limestone") public static boolean enableLimestone = true;
	@Config(flag = "jasper") public static boolean enableJasper = true;
	@Config(flag = "slate") public static boolean enableSlate = true;
	@Config(flag = "basalt") public static boolean enableBasalt = true;
	
	@Config public static StoneTypeConfig marble = new StoneTypeConfig(false);
	@Config public static StoneTypeConfig limestone = new StoneTypeConfig(false);
	@Config public static StoneTypeConfig jasper = new StoneTypeConfig(false);
	@Config public static StoneTypeConfig slate = new StoneTypeConfig(false);
	@Config public static StoneTypeConfig basalt = new StoneTypeConfig(true);
	
	private Queue<Runnable> defers = new ArrayDeque<>();
	
	@Override
	public void start() {
		makeStone("marble", marble, () -> enableMarble, MaterialColor.QUARTZ);
		makeStone("limestone", limestone, () -> enableLimestone, MaterialColor.STONE);
		makeStone("jasper", jasper, () -> enableJasper, MaterialColor.RED_TERRACOTTA);
		makeStone("slate", slate, () -> enableSlate, MaterialColor.ICE);
		makeStone("basalt", basalt, () -> enableBasalt, MaterialColor.BLACK);
	}
	
	private void makeStone(String name, StoneTypeConfig config, Supplier<Boolean> enabledCond, MaterialColor color) {
		Supplier<Boolean> trueEnabledCond = () -> enabled && enabledCond.get();
		Block.Properties props = Block.Properties.create(Material.ROCK, color).hardnessAndResistance(1.5F, 6.0F);
		
		QuarkBlock normal = new QuarkBlock(name, this, ItemGroup.BUILDING_BLOCKS, props).setCondition(enabledCond);
		QuarkBlock polished = new QuarkBlock("polished_" + name, this, ItemGroup.BUILDING_BLOCKS, props).setCondition(enabledCond);
		
		VariantHandler.addSlabStairsWall(normal);
		VariantHandler.addSlabAndStairs(polished);
		
		defers.add(() ->
			WorldGenHandler.addGenerator(new OreGenerator(config.dimensions, config.oregen, normal.getDefaultState(), OreGenerator.ALL_DIMS_STONE_MATCHER, trueEnabledCond), Decoration.UNDERGROUND_ORES, WorldGenWeights.NEW_STONES)
		);
	}
	
	@Override
	public void setup() {
		while(!defers.isEmpty())
			defers.poll().run();
	}
	
}
