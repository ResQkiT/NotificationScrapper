package backend.academy.bot.commands;

import lombok.extern.slf4j.Slf4j;

@Slf4j
abstract public class Command<ReturnedType,TransmittedType > {

    public abstract ReturnedType execute(TransmittedType object);

    public ReturnedType noArgsExec(){
        return this.execute(null);
    }
}
