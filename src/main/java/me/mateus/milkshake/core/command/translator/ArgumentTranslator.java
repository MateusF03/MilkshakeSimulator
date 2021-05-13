package me.mateus.milkshake.core.command.translator;

import me.mateus.milkshake.core.milkshake.Point;
import me.mateus.milkshake.core.milkshake.SourceRegion;
import me.mateus.milkshake.core.milkshake.builder.SourceRegionBuilder;
import me.mateus.milkshake.core.utils.StringComparator;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArgumentTranslator {

    private final Pattern ARGUMENT_PATTERN = Pattern.compile("(?:-{1,2}|/)(\\w+)(?:[=:]?|\\s+)([^-\\s\"][^\"]*?|\"[^\"]*\")?(?=\\s+[-/][^\\d]|$)");

    private final Map<String,String> mappedArguments = new HashMap<>();

    public ArgumentTranslator(String rawArguments) {
        if (!rawArguments.isEmpty() && rawArguments.contains(" ")) {
            Matcher matcher = ARGUMENT_PATTERN.matcher(rawArguments);
            while (matcher.find()) {
                if (matcher.groupCount() < 2)
                    continue;
                mappedArguments.put(matcher.group(1),matcher.group(2));
            }
        }
    }

    public int length() {
        return mappedArguments.size();
    }

    public boolean hasNoArgument(String argument) {
        return !mappedArguments.containsKey(argument);
    }

    public String getAsString(String argument) {
        return mappedArguments.get(argument);
    }

    public int getAsInteger(String argument) {
        String value = mappedArguments.get(argument);
        if (!StringComparator.isInteger(value)) {
            return 0;
        }
        return Integer.parseInt(value);
    }

    public boolean getAsBoolean(String argument) {
        return Boolean.parseBoolean(argument);
    }

    public List<SourceRegion> getAsRegions(String key) {
        String argument = mappedArguments.get(key);
        return toRegions(argument);
    }

    public List<SourceRegion> toRegions(String value) {
        List<SourceRegion> regions = new ArrayList<>();
        String[] args = value.split("[|]");
        for (String arg : args) {
            String[] values = arg.trim().split("\\s+");
            if (values.length < 5)
                continue;
            Point[] points;
            SourceRegionBuilder builder;
            try {
                int sourceIdx = Integer.parseInt(values[4]);
                if (isPointValue(values)) {
                    List<Point> pointList = new ArrayList<>();
                    for (int i = 0; i < 4; i++) {
                        pointList.add(stringToPoint(values[i]));
                    }
                    points = pointList.toArray(new Point[0]);
                    builder = new SourceRegionBuilder(sourceIdx, points);
                } else {
                    int x = Integer.parseInt(values[0]);
                    int y = Integer.parseInt(values[1]);
                    int width = Integer.parseInt(values[2]);
                    int height = Integer.parseInt(values[3]);
                    builder = new SourceRegionBuilder(sourceIdx,x,y,width,height);
                }
                if (values.length >= 6) {
                    builder.setText(Boolean.parseBoolean(values[5]));
                }
                if (values.length >= 7) {
                    builder.setPriority(Integer.parseInt(values[6]));
                }
                if (values.length >=  8) {
                    builder.setColor(values[7]);
                }
                if (values.length >=  9) {
                    builder.setOrientation(values[8]);
                }
                if (values.length >=  10) {
                    builder.setFont(values[9]);
                }
                if (values.length >=  11) {
                    builder.setStrokeColor(values[10]);
                }
                if (values.length >=  12) {
                    builder.setStrokeWidth(Integer.parseInt(values[6]));
                }
                SourceRegion region = builder.build();
                regions.add(builder.build());
            } catch (NumberFormatException e) {
                return Collections.emptyList();
            }
        }
        return regions;
    }

    private boolean isPointValue(String[] values) {
        for (int i = 0; i < 4; i++) {
            if (!isPoint(values[i])) {
                return false;
            }
        }
        return true;
    }

    private boolean isPoint(String value) {
        String[] a = value.split("[,.x]");
        if (a.length != 2)
            return false;
        return StringComparator.isInteger(a[0]) && StringComparator.isInteger(a[1]);
    }

    private Point stringToPoint(String value) {
        String[] a = value.split("[,.x]");
        return new Point(Integer.parseInt(a[0]), Integer.parseInt(a[1]));
    }
}
