package ru.itis.vvot.cloudphoto;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CloudphotoApplication implements ExitCodeGenerator {
    @Setter(onMethod_ = @Autowired)
    private Runner runner;

    public static void main(String[] args) {
        System.exit(SpringApplication
                .exit(SpringApplication.run(CloudphotoApplication.class, args)));
    }

    @Override
    public int getExitCode() {
        return runner.getExitCode();
    }
}
