package ru.noloverme.nvanish.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtils {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern HEX_PATTERN_ALT = Pattern.compile("&x&([A-Fa-f0-9])&([A-Fa-f0-9])&([A-Fa-f0-9])&([A-Fa-f0-9])&([A-Fa-f0-9])&([A-Fa-f0-9])");
    private static final Pattern HEX_PATTERN_SECTION = Pattern.compile("§x§([A-Fa-f0-9])§([A-Fa-f0-9])§([A-Fa-f0-9])§([A-Fa-f0-9])§([A-Fa-f0-9])§([A-Fa-f0-9])");

    /**
     * Преобразует текст с цветовыми кодами в цветной текст
     * @param message Исходный текст с кодами цветов
     * @return Цветной текст
     */
    public static String colorize(String message) {
        if (message == null) return "";
        
        // Обработка HEX формата &#FFFFFF
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();
        
        while (matcher.find()) {
            String hexColor = matcher.group(1);
            matcher.appendReplacement(buffer, "§x§" + hexColor.charAt(0) + "§" + hexColor.charAt(1) + "§" + 
                                            hexColor.charAt(2) + "§" + hexColor.charAt(3) + "§" + 
                                            hexColor.charAt(4) + "§" + hexColor.charAt(5));
        }
        
        matcher.appendTail(buffer);
        message = buffer.toString();
        
        // Обработка HEX формата &x&F&F&F&F&F&F
        matcher = HEX_PATTERN_ALT.matcher(message);
        buffer = new StringBuffer();
        
        while (matcher.find()) {
            matcher.appendReplacement(buffer, "§x§" + matcher.group(1) + "§" + matcher.group(2) + "§" + 
                                            matcher.group(3) + "§" + matcher.group(4) + "§" + 
                                            matcher.group(5) + "§" + matcher.group(6));
        }
        
        matcher.appendTail(buffer);
        message = buffer.toString();
        
        // Обработка стандартных цветовых кодов &
        message = message.replace('&', '§');
        
        return message;
    }

    /**
     * Преобразует текст с цветовыми кодами в компонент Adventure API
     * @param message Исходный текст с кодами цветов
     * @return Компонент Adventure API
     */
    public static Component colorizeComponent(String message) {
        if (message == null) return Component.empty();
        
        // Преобразуем все HEX и стандартные цвета
        String colorized = colorize(message);
        
        // Преобразуем в компонент через Adventure API
        return LegacyComponentSerializer.legacySection().deserialize(colorized);
    }
    
    /**
     * Преобразует MiniMessage формат в цветной текст
     * @param message Исходный текст в формате MiniMessage
     * @return Цветной текст
     */
    public static Component miniMessage(String message) {
        if (message == null) return Component.empty();
        
        return MiniMessage.miniMessage().deserialize(message);
    }
    
    /**
     * Удаляет все цветовые коды из текста
     * @param message Исходный текст с кодами цветов
     * @return Текст без цветовых кодов
     */
    public static String stripColor(String message) {
        if (message == null) return "";
        
        // Удаляем HEX формат &#FFFFFF
        message = HEX_PATTERN.matcher(message).replaceAll("");
        
        // Удаляем HEX формат &x&F&F&F&F&F&F
        message = HEX_PATTERN_ALT.matcher(message).replaceAll("");
        
        // Удаляем HEX формат §x§F§F§F§F§F§F
        message = HEX_PATTERN_SECTION.matcher(message).replaceAll("");
        
        // Удаляем стандартные цветовые коды
        message = message.replaceAll("(?i)[§&][0-9A-FK-ORX]", "");
        
        return message;
    }
}
