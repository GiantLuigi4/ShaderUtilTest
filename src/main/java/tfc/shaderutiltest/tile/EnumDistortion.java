package tfc.shaderutiltest.tile;

import net.minecraft.util.StringIdentifiable;

public enum EnumDistortion implements StringIdentifiable {
	NONE,
	BETWEENLANDS,
//	SURROUND,
//	BEHIND,
	;
	
	
	@Override
	public String asString() {
		return name().toLowerCase();
	}
}
