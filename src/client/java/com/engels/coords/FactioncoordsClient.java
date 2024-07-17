package com.engels.coords;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FactioncoordsClient implements ClientModInitializer {
	public static String world=null;
	public static Path originPath=null;
	public static Path locationPath=null;
	private float totalTickDelta;
	public static boolean showhud=false;
	@Override
	public void onInitializeClient() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
				ClientCommandManager.literal("init")
						.then(ClientCommandManager.argument("Save name", StringArgumentType.string())
								.suggests(getInitSuggestions())
						.executes(context -> {
							world=StringArgumentType.getString(context, "Save name");
							Path savePath = createDir(world);
							if(savePath==null){
								context.getSource().sendError(Text.literal("An error occurred"));
								return 0;
							}

                            try {
                                originPath=savePath.resolve("origin.txt");
								locationPath=savePath.resolve("location.txt");
								File f1=new File(originPath.toString());
								File f2=new File(locationPath.toString());
								if(!f1.exists()){
									Files.createFile(originPath);
								}
								if(!f2.exists()){
									Files.createFile(locationPath);
								}
								context.getSource().sendFeedback(Text.literal("World initialised successfully!"));

                            } catch (IOException e) {
                                e.printStackTrace();
								context.getSource().sendError(Text.literal("An error occurred"));
								return 0;
                            }



							return 1;
						})
		)));

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
				ClientCommandManager.literal("hello")
						.executes(context -> {
							showhud=true;
							context.getSource().sendFeedback(Text.literal("Hello"));
							if(world==null){
								context.getSource().sendError(Text.literal("Looks like this world is not initialised. do it with /init <save name>"));
							}
							return 1;
						})
		));
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
				ClientCommandManager.literal("hud")
						.executes(context -> {
							showhud=!showhud;
							return 1;
						})
		));
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
				ClientCommandManager.literal("originset")
						.executes(context -> {
							if(world==null){
								Text message = Text.literal("Looks like this world is not initialised. do it with /init <save name>");
								context.getSource().sendError(message);
								return 0;
							}
							BlockPos position=context.getSource().getPlayer().getBlockPos();
							Identifier dimensionId = context.getSource().getPlayer().getEntityWorld().getRegistryKey().getValue();
							if (dimensionId.equals(0)){
								Text message = Text.literal("Origin can only be saved in overworld");
								context.getSource().sendError(message);
								return 0;
							}

							File origin=new File(originPath.toString());
                            try {
                                BufferedWriter br = new BufferedWriter(new FileWriter(origin));
								br.write(position.getX() + " " + position.getY() + " " + position.getZ() + "\n");
								br.close();
                            } catch (IOException e) {
								e.printStackTrace();
								context.getSource().sendError(Text.literal("An error occurred"));
								return 0;
                            }


							context.getSource().sendFeedback(Text.literal("Origin saved!"));
							return 1;
						})
		));
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
				ClientCommandManager.literal("originget")
						.executes(context -> {
							if(world==null){
								Text message = Text.literal("Looks like this world is not initialised. do it with /init <save name>");
								context.getSource().sendError(message);
								return 0;
							}

							File origin=new File(originPath.toString());
							if (!origin.exists()){
								Text message = Text.literal("Looks like this world is not initialised. do it with /init <save name>");
								context.getSource().sendError(message);
								return 0;
							}
							List<String> data=new ArrayList<String>();
							try {

								Scanner myReader = new Scanner(origin);
								while (myReader.hasNextLine()) {
									data.add(myReader.nextLine());
									System.out.println(data);
								}
								myReader.close();
								if(data.isEmpty()){
									Text message = Text.literal("Origin is not set. Do it with /originset");
									context.getSource().sendError(message);
									return 0;
								}
								context.getSource().sendFeedback(Text.literal(data.getFirst()));
							} catch (FileNotFoundException e) {
								Text message = Text.literal("Read error");
								context.getSource().sendError(message);
								e.printStackTrace();
								return 0;
							}


							return 1;
						})
		));
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
				ClientCommandManager.literal("raw")
						.executes(context -> {
							BlockPos position=context.getSource().getPlayer().getBlockPos();
							Text message = Text.literal(position.getX() + " " + position.getY() + " " + position.getZ() + "\n");
							context.getSource().sendFeedback(message);
							return 1;
						})
		));
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
				ClientCommandManager.literal("coords")
						.executes(context -> {
							if(world==null){
								Text message = Text.literal("Looks like this world is not initialised. do it with /init <save name>");
								context.getSource().sendError(message);
								return 0;
							}

							File origin=new File(originPath.toString());
							if (!origin.exists()){
								Text message = Text.literal("Looks like this world is not initialised. do it with /init <save name>");
								context.getSource().sendError(message);
								return 0;
							}
							List<String> data=new ArrayList<String>();
							try {

								Scanner myReader = new Scanner(origin);
								while (myReader.hasNextLine()) {
									data.add(myReader.nextLine());
								}
								myReader.close();
								if(data.isEmpty()){
									Text message = Text.literal("Origin is not set. Do it with /originset");
									context.getSource().sendError(message);
									return 0;
								}

								String[] originSplit= data.getFirst().split(" ",3);
								List<Integer> coords = new ArrayList<Integer>();
								for(String s : originSplit) coords.add(Integer.valueOf(s));

								BlockPos position=context.getSource().getPlayer().getBlockPos();

								String coordinates=String.valueOf((int)position.getX()-coords.get(0))+" "+String.valueOf((int)position.getY()-coords.get(1))+" "+String.valueOf((int)position.getZ()-coords.get(2));
								Text message = Text.literal("Showing faction coordinates:\n")
										.append(Text.literal("[").setStyle(Style.EMPTY.withFormatting(Formatting.GREEN)).append(Text.literal(coordinates).setStyle(Style.EMPTY.withFormatting(Formatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD,coordinates ))).append("]")));
								context.getSource().sendFeedback(message);
							} catch (FileNotFoundException e) {
								Text message = Text.literal("Read error");
								context.getSource().sendError(message);
								e.printStackTrace();

							}
                            return 0;
                        })
		));
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
				ClientCommandManager.literal("convert")
						.then(ClientCommandManager.argument("Coordinates", StringArgumentType.greedyString())
						.executes(context -> {
							String rawInput = StringArgumentType.getString(context, "Coordinates");
							System.out.println(rawInput);
							List<Integer> local = new ArrayList<Integer>();
							try{
								String[] inputSplit= rawInput.split(" ",3);

								for(String s : inputSplit) local.add(Integer.valueOf(s));
							}catch(Exception e){
								Text message = Text.literal("Incorrect input!\n");
								context.getSource().sendError(message);
								return 1;
							}
							if(world==null){
								Text message = Text.literal("Looks like this world is not initialised. do it with /init <save name>");
								context.getSource().sendError(message);
								return 0;
							}

							File origin=new File(originPath.toString());
							if (!origin.exists()){
								Text message = Text.literal("Looks like this world is not initialised. do it with /init <save name>");
								context.getSource().sendError(message);
								return 0;
							}
							List<String> data=new ArrayList<String>();
							try {

								Scanner myReader = new Scanner(origin);
								while (myReader.hasNextLine()) {
									data.add(myReader.nextLine());
								}
								myReader.close();
								if(data.isEmpty()){
									Text message = Text.literal("Origin is not set. Do it with /originset");
									context.getSource().sendError(message);
									return 0;
								}

								String[] originSplit= data.getFirst().split(" ",3);
								List<Integer> coords = new ArrayList<Integer>();
								for(String s : originSplit) coords.add(Integer.valueOf(s));

								BlockPos position=context.getSource().getPlayer().getBlockPos();

								String coordinates=String.valueOf((int)local.get(0)+coords.get(0))+" "+String.valueOf((int)local.get(1)+coords.get(1))+" "+String.valueOf((int)local.get(2)+coords.get(2));
								Text message = Text.literal("Showing local coordinates:\n")
										.append(Text.literal("[").setStyle(Style.EMPTY.withFormatting(Formatting.GREEN)).append(Text.literal(coordinates).setStyle(Style.EMPTY.withFormatting(Formatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD,coordinates ))).append("]")));
								context.getSource().sendFeedback(message);
							} catch (FileNotFoundException e) {
								Text message = Text.literal("Read error");
								context.getSource().sendError(message);
								e.printStackTrace();

							}
							return 0;
						})
		)));

		HudRenderCallback.EVENT.register((context, tickDeltaManager) -> {
			PlayerEntity player = MinecraftClient.getInstance().player;
			double playerX = player.getX();
			double playerY = player.getY();
			double playerZ = player.getZ();
			TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
			if(showhud) return;
			if(world==null){
				Text message = Text.literal("Looks like this world is not initialised. do it with /init <save name>");
				context.drawText(renderer,message,0,0,0x00ff00,true);
				return;
			}

			File origin=new File(originPath.toString());
			if (!origin.exists()){
				Text message = Text.literal("Looks like this world is not initialised. do it with /init <save name>");
				context.drawText(renderer,message,0,0,0x00ff00,true);
				return;
			}
			List<String> data=new ArrayList<String>();
			try {

				Scanner myReader = new Scanner(origin);
				while (myReader.hasNextLine()) {
					data.add(myReader.nextLine());
				}
				myReader.close();
				if(data.isEmpty()){
					Text message = Text.literal("Origin is not set. Do it with /originset");
					context.drawText(renderer,message,0,0,0x00ff00,true);
					return;
				}
				//context.getSource().sendFeedback(Text.literal(data.getFirst()));
			} catch (FileNotFoundException e) {
				Text message = Text.literal("Read error");
				context.drawText(renderer,message,0,0,0x00ff00,true);
				return;
			}
			String[] originSplit= data.getFirst().split(" ",3);
			List<Integer> coords = new ArrayList<Integer>();
			for(String s : originSplit) coords.add(Integer.valueOf(s));


			context.drawText(renderer,String.valueOf((int)playerX - coords.get(0))+" "+String.valueOf((int)playerY - coords.get(1))+" "+String.valueOf((int)playerZ - coords.get(2)),0,0,0xffffff,true);
		});

	}

	private SuggestionProvider<FabricClientCommandSource> getInitSuggestions() {
		return (context, builder) -> {
			List<String> suggestions = getFolderNames(Paths.get("config").resolve("faction-coords").toString());
			return CommandSource.suggestMatching(suggestions, builder);
		};
	}
	public static List<String> getFolderNames(String directoryPath)  {
		List<String> folderNames = new ArrayList<>();

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(directoryPath))) {
			for (Path entry : stream) {
				if (Files.isDirectory(entry)) {
					folderNames.add(entry.getFileName().toString());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return folderNames;
	}

	private static Path createDir(String SaveName) {
		try{
			Path savePath=getSavePath(SaveName);

			if (!Files.exists(savePath)) {
				Files.createDirectories(savePath);
			}
			return savePath;

		}catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}


	private static Path getSavePath(String SaveName) {

			return Paths.get("config").resolve("faction-coords").resolve(SaveName);

	}
}