package backend.academy.bot.commands;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StartCommand extends Command<String, Object>{

    @Override
    public String execute(Object object) {
        log.info("Start command");

        return  "Вызвана команда для старта + \n" +
                "Чтобы зарегистрироваться в боте, выполните команду /register";
    }
}
