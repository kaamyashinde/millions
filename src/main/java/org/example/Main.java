package org.example;

import cli.UserInterface;

/**
 * Entry point for the Millions stock trading application.
 * Launches the command-line interface.
 */
public class Main {

  /**
   * Main method. Delegates to the CLI.
   *
   * @param args command-line arguments (unused)
   */
  public static void main(String[] args) {
    UserInterface.launch();
  }
}
