package ru.itis.vvot.cloudphoto.other;

import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.UnmatchedArgumentException;

import java.io.PrintWriter;

@Component
public class ErrorMessageHandler implements CommandLine.IParameterExceptionHandler {
    @Override
    public int handleParseException(ParameterException ex, String[] args) {
        CommandLine cmd = ex.getCommandLine();
        PrintWriter err = cmd.getErr();

//        // if tracing at DEBUG level, show the location of the issue
//        if ("DEBUG".equalsIgnoreCase(System.getProperty("picocli.trace"))) {
//            err.println(cmd.getColorScheme().stackTraceText(ex));
//        }

        err.println(cmd.getColorScheme().errorText(ex.getMessage().replaceAll("=", " "))); // bold red
        UnmatchedArgumentException.printSuggestions(ex, err);
        err.print(cmd.getHelp().fullSynopsis().replaceAll("=", " "));

        CommandSpec spec = cmd.getCommandSpec();
        err.printf("Try '%s --help' for more information.%n", spec.qualifiedName());

        return cmd.getExitCodeExceptionMapper() != null
                ? cmd.getExitCodeExceptionMapper().getExitCode(ex)
                : spec.exitCodeOnInvalidInput();
    }
}
