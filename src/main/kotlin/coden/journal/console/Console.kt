package coden.journal.console

import coden.journal.core.persistance.JournalEntry
import coden.journal.core.persistance.JournalRepository
import coden.journal.core.persistance.JournalWriter
import coden.journal.core.request.Trigger
import coden.journal.core.request.UI
import org.apache.logging.log4j.kotlin.Logging
import java.time.YearMonth

// ideally Spring shell
class Console(
    private val ui: UI,
    private val repository: JournalRepository
): Trigger, JournalWriter, Logging {

    override fun start() {
        logger.info { "Starting console" }
        while (true){
            try {
                val run = readCommand()
                if (!run){break}
            }catch (e: Exception){
                println("Error: "+e.message)
            }
        }
    }

    private fun readCommand(): Boolean {
        println("")
        println("[trigger [<YYYY-mm>]]")
        println("[write <YYYY-mm> <description>]")
        println("[exit]")
        print("> ")
        val command = readlnOrNull()
        if (command.isNullOrBlank()) {
            println("Command must be not be blank")
            return true
        }
        val args = command.split(" ", limit = 3)
        if (args[0].contains("trigger")) {
            if (args.size == 2){
                val month = YearMonth.parse(args[1])
                triggerRequest(month)
            }else{
                triggerRequest(YearMonth.now())
            }
        }

        if (args[0].contains("write")) {
            if (args.size != 3){
                println("Invalid command format for 'write'")
            }else{
                val month = YearMonth.parse(args[1])
                val descrption = args[2]
                repository.insert(JournalEntry(month, descrption))
            }
        }

        return !args[0].contains("exit")
    }

    private fun triggerRequest(month: YearMonth){
        logger.info { "Triggering for $month" }
        try {
            ui.request(month)
        } catch (e: Exception) {
            logger.error("Error while triggering occurred", e) // for debugging
            ui.error(e)                                             // for user
        }
    }

}