package org.jupiter.monitor;

import java.util.Map;
import org.jupiter.common.util.Maps;
import org.jupiter.monitor.handler.AddressHandler;
import org.jupiter.monitor.handler.AuthHandler;
import org.jupiter.monitor.handler.ByAddressHandler;
import org.jupiter.monitor.handler.ByServiceHandler;
import org.jupiter.monitor.handler.CommandHandler;
import org.jupiter.monitor.handler.HelpHandler;
import org.jupiter.monitor.handler.JStackHandler;
import org.jupiter.monitor.handler.LsHandler;
import org.jupiter.monitor.handler.MemoryUsageHandler;
import org.jupiter.monitor.handler.MetricsHandler;
import org.jupiter.monitor.handler.QuitHandler;
import org.jupiter.monitor.handler.RegistryHandler;














public enum Command
{
  AUTH("Login with password", new AuthHandler(), new ChildCommand[0]), 
  HELP("Help information", new HelpHandler(), new ChildCommand[0]), 
  STACK("Prints java stack traces of java threads for the current java process", new JStackHandler(), new ChildCommand[0]), 
  MEMORY_USAGE("Prints memory usage for the current java process", new MemoryUsageHandler(), new ChildCommand[0]), 
  LS("List all provider and consumer info", new LsHandler(), new ChildCommand[0]), 
  METRICS("Performance metrics", new MetricsHandler(), new ChildCommand[] { ChildCommand.REPORT }), 
  
  REGISTRY("Registry info(P/S command must follow behind ADDRESS)", new RegistryHandler(), new ChildCommand[] { ChildCommand.ADDRESS, ChildCommand.P, ChildCommand.S, ChildCommand.BY_SERVICE, ChildCommand.BY_ADDRESS, ChildCommand.GREP }), 
  





  QUIT("Quit monitor", new QuitHandler(), new ChildCommand[0]);
  
  private final String description;
  private final CommandHandler handler;
  private final ChildCommand[] children;
  private static final Map<String, Command> commands;
  
  private Command(String description, CommandHandler handler, ChildCommand... children) { this.description = description;
    this.handler = handler;
    this.children = children;
  }
  
  public String description() {
    return description;
  }
  
  public CommandHandler handler() {
    return handler;
  }
  
  public ChildCommand[] children() {
    return children;
  }
  
  public ChildCommand parseChild(String childName) {
    if (childName.indexOf('-') == 0) {
      childName = childName.substring(1);
    }
    for (ChildCommand c : children()) {
      if (c.name().equalsIgnoreCase(childName)) {
        return c;
      }
    }
    return null;
  }
  

  public static Command parse(String name) { return (Command)commands.get(name.toLowerCase()); }
  
  static {
    commands = Maps.newHashMap();
    

    for (Command c : values()) {
      commands.put(c.name().toLowerCase(), c);
    }
  }
  
  public static enum ChildCommand {
    REPORT("Report the current values of all metrics in the registry", null), 
    ADDRESS("List all publisher/subscriber's addresses", new AddressHandler()), 
    BY_SERVICE("List all providers by service name", new ByServiceHandler()), 
    BY_ADDRESS("List all services by addresses", new ByAddressHandler()), 
    P("Publisher", null), 
    S("Subscriber", null), 
    GREP("Search for pattern in each line", null);
    
    private final String description;
    private final CommandHandler handler;
    
    private ChildCommand(String description, CommandHandler handler) {
      this.description = description;
      this.handler = handler;
    }
    
    public String description() {
      return description;
    }
    
    public CommandHandler handler() {
      return handler;
    }
  }
}
