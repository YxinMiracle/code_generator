package com.yxinmiracle.cli;

/*
 * @author  YxinMiracle
 * @date  2024-06-08 14:55
 * @Gitee: https://gitee.com/yxinmiracle
 * 作为命令行的一个调用者
 */

import cn.hutool.core.util.ReflectUtil;
import com.yxinmiracle.cli.command.ConfigCommand;
import com.yxinmiracle.cli.command.GenerateCommand;
import com.yxinmiracle.cli.command.ListCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Command(name = "yxinmiracle", mixinStandardHelpOptions = true)
public class CommandExecutor implements Runnable {

    private final CommandLine commandLine;

    {
        commandLine = new CommandLine(this)
                .addSubcommand(new GenerateCommand())
                .addSubcommand(new ConfigCommand())
                .addSubcommand(new ListCommand());
    }

    @Override
    public void run() {
        // 不输入子命令时，给出友好提示
        System.out.println("请输入具体命令，或者输入 --help 查看命令提示");
    }

    /**
     * 这个是判断用户输入的命令中有没有没有给interactive的参数，要是没有就返回true，表示需要
     *
     * @param names   命令的指令形式
     * @param argList 用户输入的内容
     * @return 需要需要，表示用户输入的少了，那就返回true，反之就是false
     */
    private boolean isNeed(String[] names, List<String> argList) {
        boolean flag = false;
        for (String name : names) {
            for (String s : argList) {
                if (name.equals(s)){
                    flag = true;
                }
            }
        }
        return !flag?true:false;
    }

    /**
     * 执行命令
     *
     * @param args
     * @return
     */
    public Integer doExecute(String[] args) {
        List<String> argList = Arrays.asList(args).stream().filter(s -> {
            return s.startsWith("-");
        }).collect(Collectors.toList());
        Field[] fields = ReflectUtil.getFields(GenerateCommand.class);
        List<String> needAddCommand = new ArrayList<>();
        for (Field field : fields) {
            Option fieldAnnotation = field.getAnnotation(Option.class);
            boolean interactive = fieldAnnotation.interactive();
            if (!interactive) continue; // 如果interactive不为true那么就不管
            // 如果说这个参数是需要进行交互的，那么就进来
            String[] names = fieldAnnotation.names();
            boolean need = isNeed(names, argList);
            if (!need) continue; // 如果说不需要，那也可以不管
            needAddCommand.add(names[0]);
        }
        String[] newArgs = new String[args.length + needAddCommand.size()];
        for (int i = 0; i < args.length; i++) {
            newArgs[i] = args[i];
        }
        for (int i = args.length,j=0; i < args.length + needAddCommand.size(); i++,j++) {
            newArgs[i] = needAddCommand.get(j);
        }
        System.out.println(Arrays.toString(newArgs));
        return commandLine.execute(newArgs);
    }
}