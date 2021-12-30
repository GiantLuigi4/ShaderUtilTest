package tfc.shaderutiltest.tile;

import net.minecraft.util.StringIdentifiable;

public enum EnumShader implements StringIdentifiable {
	NONE,
	BETWEENLANDS,
	GRAYSCALE,
	DEPTH,
	GBRA,
	FLIP,
//	SURROUND,
//	BEHIND,
	;
	
	
	@Override
	public String asString() {
		return name().toLowerCase();
	}
}
