TagPlugin

TagPlugin is a Bukkit plugin that adds a multiplayer tag game to your server. Engage your players in an exciting chase where they can tag each other within the virtual world. This README file provides instructions for installing and using the plugin.



Installation

Download the latest version of TagPlugin from the releases page.

Place the downloaded JAR file into the "plugins" folder of your Bukkit server.

Start or restart your server.



Usage


TagPlugin provides the following commands:

/starttag - Start a new tag game.

/stoptag - Stop the current tag game.

/tagleaderboard - View the leaderboard of top players who have tagged others the most.

Players can tag each other by physically coming into contact within the game. When a player gets tagged or successfully tags someone, customizable messages will be displayed to enhance the gameplay experience.



Configuration

The plugin comes with a configuration file that allows you to customize the in-game messages. To modify the messages, follow these steps:



Locate the "config.yml" file in the "plugins/TagPlugin" folder.

Open the file using a text editor.

Update the values for the following message keys according to your preference:

tag_message - Message displayed to a player when they get tagged.

tagger_message - Message displayed to a player when they successfully tag someone.

console_message - Message sent to the server console when a tag event occurs.

global_message - Message broadcasted to all players (except the tagger and tagged) when a tag event occurs.



Permissions

TagPlugin uses the following permissions:



tagplugin.start - Allows players to start a tag game using the /starttag command.

tagplugin.stop - Allows players to stop an ongoing tag game using the /stoptag command.

Ensure that you have a permissions plugin installed and properly configured to manage these permissions.



Support and Issues

If you encounter any issues or need assistance with the TagPlugin, you can:



Check the documentation for more information.

Report issues or bugs on the issue tracker.


Contributing

Contributions to TagPlugin are welcome! If you'd like to contribute to the development or have any suggestions, feel free to submit pull requests or reach out to us.



License

TagPlugin is released under the MIT License.



Note: This plugin is developed by zDr4l and is not affiliated with or endorsed by Bukkit or its contributors.
