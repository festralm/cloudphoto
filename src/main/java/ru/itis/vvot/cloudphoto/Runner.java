package ru.itis.vvot.cloudphoto;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.ParseResult;
import ru.itis.vvot.cloudphoto.command.CloudphotoCommand;
import ru.itis.vvot.cloudphoto.other.ErrorMessageHandler;

import java.util.Optional;

import static picocli.CommandLine.Model.UsageMessageSpec.SECTION_KEY_OPTION_LIST;
import static picocli.CommandLine.Model.UsageMessageSpec.SECTION_KEY_SYNOPSIS;

@Component
public class Runner implements CommandLineRunner {
    @Setter(onMethod_ = @Autowired)
    private CommandLine.IFactory factory;

    @Setter(onMethod_ = @Autowired)
    private CloudphotoCommand cloudphotoCommand;

    @Setter(onMethod_ = @Autowired)
    private ErrorMessageHandler errorMessageHandler;

    private int exitCode;

    @Override
    public void run(String... args) {
        CommandLine command = new CommandLine(cloudphotoCommand, factory)
                .setParameterExceptionHandler(errorMessageHandler);

        for (CommandLine cmd : command.getSubcommands().values()) {
            cmd.getHelpSectionMap().put(SECTION_KEY_OPTION_LIST, help -> help.optionList().replaceAll("=", " "));
            cmd.getHelpSectionMap().put(SECTION_KEY_SYNOPSIS, help -> help.synopsis(help.synopsisHeadingLength()).replaceAll("=", " "));
        }
        checkCommands(command, args);
        if (exitCode == 0) {
            exitCode = command.execute(args);
        }

        if (exitCode != 0) {
            exitCode = 1;
        }
    }

    private void checkCommands(CommandLine command, String... args) {
        try {
            ParseResult parseResult = command.parseArgs(args);
            if (parseResult.originalArgs().size() == 0) {
                exitCode = 1;
                System.err.println("Specify subcommand or option for cloudphoto.\nTry 'cloudphoto --help' for more information.");
                return;
            }

            Optional<CommandLine> calledSubcommand = Optional.empty();
            for (ParseResult x : parseResult.subcommands()) {
                if (x.originalArgs().size() > 2) {
                    calledSubcommand = command.getSubcommands()
                            .values()
                            .stream()
                            .filter(y -> y.getCommandSpec().equals(x.commandSpec())
                                    && y.isUsageHelpRequested())
                            .findFirst();
                    break;
                }
            }

            if (command.isUsageHelpRequested() && parseResult.originalArgs().size() > 1
                    || calledSubcommand.isPresent()) {
                exitCode = 1;
                System.err.println("Wrong usage of --help option");
            }
        } catch (Exception i) {
        }
    }

    public int getExitCode() {
        return exitCode;
    }
}
