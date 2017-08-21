package main.java.de.frigerius.frostbot;

public class ColoredText
{
	public static String red(String text)
	{
		return color(text, "red");
	}

	public static String green(String text)
	{
		return color(text, "green");
	}

	public static String color(String text, String color)
	{
		return String.format("[color=%s]%s[/color]", color, text);
	}
}
