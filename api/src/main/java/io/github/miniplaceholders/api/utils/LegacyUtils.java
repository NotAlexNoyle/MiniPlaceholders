package io.github.miniplaceholders.api.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

/**
 * Legacy utils
 */
public final class LegacyUtils {
    private LegacyUtils() {
    }

    /**
     * Legacy serializer with hex support
     * <p>Use this as the last available alternative,
     * it is recommended to use MiniMessage instead of this serializer</p>
     */
    public static final LegacyComponentSerializer LEGACY_HEX_SERIALIZER = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .build();

    /**
     * Parse a string with possible legacy Ampersand/Section symbols
     * <br>
     * <p><b>
     * This method should only be used in case a String is provided from a legacy plugin/mod
     * or legacy source, where it is very likely to get a legacy string.
     * It is not recommended to use this method regularly in any sense
     * </b></p>
     *
     * @param string the string
     * @return a parsed string
     * @since 1.0.0
     */
    public static @NotNull Component parsePossibleLegacy(@Nullable String string) {
        if (string == null || string.isEmpty()) return Component.empty();
        if (string.indexOf(LegacyComponentSerializer.SECTION_CHAR) != -1) {
            string = string.replace(LegacyComponentSerializer.SECTION_CHAR, LegacyComponentSerializer.AMPERSAND_CHAR);
        }
        if (string.indexOf(LegacyComponentSerializer.AMPERSAND_CHAR) == -1) {
            return miniMessage().deserialize(string);
        }
        return miniMessage().deserialize(
                miniMessage().serialize(LEGACY_HEX_SERIALIZER.deserialize(string))
                        .replace("\\<", "<")
                        .replace("\\>", ">")
        );
    }

    /**
     * Parse a string with possible legacy Ampersand/Section symbols
     * using a parsing Context
     * <p><b>
     * This method should only be used in case a String is provided from a legacy plugin/mod
     * or legacy source, where it is very likely to get a legacy string.
     * It is not recommended to use this method regularly in any sense
     * </b></p>
     *
     * @param string  the string
     * @param context the provided parsing context
     * @return a parsed string
     * @since 2.2.1
     */
    public static @NotNull Component parsePossibleLegacy(@Nullable String string, @NotNull final Context context) {
        if (string == null || string.isEmpty()) return Component.empty();
        if (string.indexOf(LegacyComponentSerializer.SECTION_CHAR) != -1) {
            string = string.replace(LegacyComponentSerializer.SECTION_CHAR, LegacyComponentSerializer.AMPERSAND_CHAR);
        }
        if (string.indexOf(LegacyComponentSerializer.AMPERSAND_CHAR) == -1) {
            return context.deserialize(string);
        }
        return context.deserialize(
                miniMessage().serialize(LEGACY_HEX_SERIALIZER.deserialize(string))
                        .replace("\\<", "<")
                        .replace("\\>", ">")
        );
    }

    /**
     * Parse a string with possible legacy Ampersand/Section symbols
     * using the provided resolver
     * <p><b>
     * This method should only be used in case a String is provided from a legacy plugin/mod
     * or legacy source, where it is very likely to get a legacy string.
     * It is not recommended to use this method regularly in any sense
     * </b></p>
     *
     * @param string   the string
     * @param resolver a resolver
     * @return a parsed string
     * @since 2.2.1
     */
    public static @NotNull Component parsePossibleLegacy(@Nullable String string, @NotNull final TagResolver resolver) {
        if (string == null || string.isEmpty()) return Component.empty();
        if (string.indexOf(LegacyComponentSerializer.SECTION_CHAR) != -1) {
            string = string.replace(LegacyComponentSerializer.SECTION_CHAR, LegacyComponentSerializer.AMPERSAND_CHAR);
        }
        if (string.indexOf(LegacyComponentSerializer.AMPERSAND_CHAR) == -1) {
            return miniMessage().deserialize(string, resolver);
        }
        return miniMessage().deserialize(
                miniMessage().serialize(LEGACY_HEX_SERIALIZER.deserialize(string))
                        .replace("\\<", "<")
                        .replace("\\>", ">"),
                resolver
        );
    }
}
