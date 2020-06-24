package it.unige.ai.speech.recognition.utils;

import java.util.HashMap;
import java.util.Set;

import it.unige.ai.speech.recognition.activities.SpeechRecognitionActivity;
import it.unige.ai.speech.synthesis.activities.CaptioningActivity;

public class CommandUtils {

    // Supported commands
    public static final String CMD_captioning = "captioning";
    public static final String CMD_textRecognition = "text recognition";
    public static final String CMD_speechRecognition = "speech recognition";
    public static final String CMD_depthEstimation = "depth estimation";
    public static final String CMD_poseDetection = "pose detection";
    public static final String CMD_takeSelfie = "take a selfie";

    private static HashMap<String, Class<?>> commandsNav;

    private CommandUtils() {

    }

    static {
        commandsNav = new HashMap<>();

        // Available commands
        commandsNav.put(CMD_captioning, CaptioningActivity.class);
        commandsNav.put(CMD_speechRecognition, SpeechRecognitionActivity.class);

        // Not available commands - null
        commandsNav.put(CMD_textRecognition, null);
        commandsNav.put(CMD_depthEstimation, null);
        commandsNav.put(CMD_poseDetection, null);
        commandsNav.put(CMD_takeSelfie, null);
    }

    public static HashMap<String, Class<?>> getCommandsNav() {
        return commandsNav;
    }

    public static Set<String> getCommands() {
        return commandsNav.keySet(); // keySet is O(1)
    }

}
