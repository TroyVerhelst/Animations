/* 
 *  Copyright (C) 2016 Ivan1pl
 * 
 *  This file is part of Animations.
 * 
 *  Animations is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Animations is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Animations.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.ivan1pl.animations.commands;

import com.ivan1pl.animations.AnimationsPlugin;
import com.ivan1pl.animations.constants.Permissions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Ivan1pl
 */
public class AnimCommand extends AnimationsCommand {
    
    public AnimCommand() {
        super(Permissions.PERMISSION_ADMIN, 1, true);
    }

    @Override
    protected void execute(CommandSender cs, String... args) {
        String name = args[0];
        AnimationsPlugin.getPluginInstance().getConversationFactory().startConversation((Player) cs, name);
    }
    
}
