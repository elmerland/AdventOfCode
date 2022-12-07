class Y22D7 : Puzzle(22, 7) {
    override fun part1(): String {
        fun getNextCommand(index: Int): Command {
            return Command(
                input[index],
                input
                    .slice(index + 1 until input.size)
                    .takeWhile { !it.startsWith("$") }
            )
        }

        var index = 0
        val commands = mutableListOf<Command>()
        while (input.indices.contains(index)) {
            val command = getNextCommand(index)
            commands.add(command)
            index += command.length()
        }

        // Build directory structure
        val rootNode = DirTree("/", null)
        var currDir = rootNode

        commands.subList(1, commands.size).forEach { command ->
            if (command.isCd()) {
                currDir = if (command.getInputArg() == "..") {
                    currDir.parent!!
                } else {
                    currDir.addDirIfAbsent(command.getInputArg())
                }
            } else {
                currDir.files.addAll(command.getFiles())
                command.getDirs().forEach { currDir.addDirIfAbsent(it) }
            }
        }

//        println(rootNode.toString())

        val result = rootNode.findDirs(true) { it.getSize() <= 100_000 }.sumOf { it.getSize() }
        return result.toString()
    }

    override fun part2(): String {
        val totalStorage = 70_000_000
        val targetUnused = 30_000_000

        fun getNextCommand(index: Int): Command {
            return Command(
                input[index],
                input
                    .slice(index + 1 until input.size)
                    .takeWhile { !it.startsWith("$") }
            )
        }

        var index = 0
        val commands = mutableListOf<Command>()
        while (input.indices.contains(index)) {
            val command = getNextCommand(index)
            commands.add(command)
            index += command.length()
        }

        // Build directory structure
        val rootNode = DirTree("/", null)
        var currDir = rootNode

        commands.subList(1, commands.size).forEach { command ->
            if (command.isCd()) {
                currDir = if (command.getInputArg() == "..") {
                    currDir.parent!!
                } else {
                    currDir.addDirIfAbsent(command.getInputArg())
                }
            } else {
                currDir.files.addAll(command.getFiles())
                command.getDirs().forEach { currDir.addDirIfAbsent(it) }
            }
        }


        val currentStorage = rootNode.getSize()
        val targetDirSize = targetUnused - (totalStorage - currentStorage)

        val dirToRemove = rootNode.findDirs { it.getSize() >= targetDirSize }.minBy { it.getSize() }

        return dirToRemove.getSize().toString()

    }

    data class Command(val cmd: String, val output: List<String>) {
        fun length() = 1 + output.size

        fun isCd() = cmd.startsWith("$ cd")

        fun getInputArg() = cmd.split(" ").takeLast(1)[0]

        fun getFiles() =
            output.filter { !it.startsWith("dir") }
                .map { it.split(" ") }
                .map { (size, name) -> File(name, size.toInt()) }

        fun getDirs() =
            output.filter { it.startsWith("dir") }
                .map { it.split(" ")[1] }
    }

    class DirTree(
        val name: String,
        val parent: DirTree?,
        val level: Int = 0,
        val files: MutableSet<File> = mutableSetOf(),
        val dirs: MutableList<DirTree> = mutableListOf(),
    ) {
        fun addDirIfAbsent(name: String) = dirs.find { it.name == name }
            ?: DirTree(name, this, level + 1).also { this.dirs.add(it) }

        fun getSize(): Int = files.sumOf { it.size } + dirs.sumOf { it.getSize() }

        fun findDirs(exclThis: Boolean = false, predicate: (DirTree) -> Boolean): List<DirTree> {
            val selectedDirs = mutableListOf<DirTree>()
            selectedDirs.addAll(dirs.map { it.findDirs(false, predicate) }.flatten())
            if (!exclThis && predicate(this)) {
                selectedDirs.add(this)
            }
            return selectedDirs
        }

        override fun toString(): String {
            val sb = StringBuilder()
            val indent = "..".repeat(level)
            sb.appendLine("$indent> $name (s=${this.getSize()})")
            dirs.sortedBy { it.name }.forEach { sb.appendLine(it.toString()) }
            files.sortedBy { it.name }.map { "$indent..$it" }.forEach { sb.appendLine(it) }
            return sb.toString().trim()
        }
    }

    data class File(val name: String, val size: Int) {
        override fun toString(): String {
            return "- $name (s=$size)"
        }
    }
}