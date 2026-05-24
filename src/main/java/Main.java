import cli.UserInterface;
import java.util.Locale;
import util.I18n;

/**
 * Entry point for the Millions stock trading application. Launches the command-line interface.
 *
 * @param args optional arguments; pass {@code nb} to use Norwegian UI strings
 */
void main(String[] args) {
  if (args.length > 0 && "nb".equalsIgnoreCase(args[0])) {
    I18n.setLocale(Locale.forLanguageTag("nb"));
  }
  UserInterface.launch();
}
