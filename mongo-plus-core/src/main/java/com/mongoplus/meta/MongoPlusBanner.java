package com.mongoplus.meta;

import com.mongoplus.enums.BannerType;

/**
 * MongoPlus Banner 打印
 */
public final class MongoPlusBanner {

    private static final int STRAP_LINE_SIZE = 50;

    private static final String MONGO_PLUS = " :: MongoPlus :: ";
    private static final String MONGO_DRIVER = " :: MongoDBDriver :: ";

    public static void printBanner(boolean isEnable, BannerType bannerType) {
        if (!isEnable) return;

        System.out.println();
        printBannerArt(bannerType);
        printBannerInfo();
        System.out.println();
    }

    private static void printBannerArt(BannerType type) {
        String[] banners = (type == BannerType.IKUN) ? BannerContent.BANNER_KK : BannerContent.BANNER;
        for (String line : banners) {
            System.out.println(line);
        }
    }

    private static void printBannerInfo() {
        String mongoPlusVersion = " (v" + MongoPlusVersion.getVersion() + ")";
        String driverVersion = " (v" + MongoPlusVersion.getActualUseMongoDriverVersion() + ")";

        System.out.println(formatLine(MONGO_PLUS, mongoPlusVersion));
        System.out.println(formatLine(MONGO_DRIVER, driverVersion));
    }

    private static String formatLine(String prefix, String version) {
        int paddingLength = STRAP_LINE_SIZE - (prefix.length() + version.length());
        StringBuilder paddingBuilder = new StringBuilder();
        for (int i = 0; i < paddingLength; i++) {
            paddingBuilder.append(" ");
        }
        return prefix + paddingBuilder + version;
    }

}
