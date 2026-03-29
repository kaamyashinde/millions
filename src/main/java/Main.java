import cli.UserInterface;
import java.util.Locale;
import util.I18n;

/**
 * Entry point for the Millions stock trading application. Launches the command-line interface.
 */
public class Main {

  /**
   * Main method. Delegates to the CLI.
   *
   * @param args optional: pass {@code nb} as the first argument to use Norwegian
   *             ({@code messages_nb.properties})
   */
  public static void main(String[] args) {
    if (args.length > 0 && "nb".equalsIgnoreCase(args[0])) {
      I18n.setLocale(Locale.forLanguageTag("nb"));
    }
    UserInterface.launch();
  }
}
