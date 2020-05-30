package com.gabe.woolwars;

public enum KitPower {
    ARROW_ON_BOW_HIT(true), ANTI_KNOCKBACK(true), SANDSTONE_PROTECTION(true);

    private boolean isFree;

    KitPower(boolean free){
        isFree = free;
    }

    public boolean isFree() {
        return isFree;
    }

    public static KitPower getPower(String text){
        if(text.equalsIgnoreCase("ARROW_ON_BOW_HIT")){
            return ARROW_ON_BOW_HIT;
        }else if(text.equalsIgnoreCase("ANTI_KNOCKBACK")){
            return ANTI_KNOCKBACK;
        }else if(text.equalsIgnoreCase("SANDSTONE_PROTECTION")){
            return SANDSTONE_PROTECTION;
        }else{
            return null;
        }
    }
}
