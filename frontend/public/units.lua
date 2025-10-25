warn(script.Name .. " loaded")

--[[

 /$$$$$$$              /$$     /$$     /$$            /$$$$$$  /$$           /$$       /$$
| $$__  $$            | $$    | $$    | $$           /$$__  $$|__/          | $$      | $$
| $$  \ $$  /$$$$$$  /$$$$$$ /$$$$$$  | $$  /$$$$$$ | $$  \__/ /$$  /$$$$$$ | $$  /$$$$$$$
| $$$$$$$  |____  $$|_  $$_/|_  $$_/  | $$ /$$__  $$| $$$$    | $$ /$$__  $$| $$ /$$__  $$
| $$__  $$  /$$$$$$$  | $$    | $$    | $$| $$$$$$$$| $$_/    | $$| $$$$$$$$| $$| $$  | $$
| $$  \ $$ /$$__  $$  | $$ /$$| $$ /$$| $$| $$_____/| $$      | $$| $$_____/| $$| $$  | $$
| $$$$$$$/|  $$$$$$$  |  $$$$/|  $$$$/| $$|  $$$$$$$| $$      | $$|  $$$$$$$| $$|  $$$$$$$
|_______/  \_______/   \___/   \___/  |__/ \_______/|__/      |__/ \_______/|__/ \_______/
                                                                                          
 /$$$$$$$  /$$$$$$$$ /$$$$$$ 
| $$__  $$|__  $$__//$$__  $$
| $$  \ $$   | $$  | $$  \__/
| $$$$$$$/   | $$  |  $$$$$$ 
| $$__  $$   | $$   \____  $$
| $$  \ $$   | $$   /$$  \ $$
| $$  | $$   | $$  |  $$$$$$/
|__/  |__/   |__/   \______/ 
                                                                                                                                                                                                                                
                                                                                                                             
---------------------------
This is the main script that runs for a match instance, from start to game end. Handles 
things such as

Map loading , Starting the Game, Spawning Units, executing a win / lose condition
and teleporting players back to the lobby

KEY ARCHITECTURAL DECISIONS:
--Maps are stored in ServerStorage instead of ReplicatedStorage for security.
--Maps contain information such as enemy spawns, and ai behaviour
--that players should not access

Instance Values
-- These are sometimes used instead of global variables
--when data must be accessible from all server and client scripts
--eg timer is stored in a IntValue, and
-- unit data is stored in values under a configuration folder
-- this is not possible with a global module like _G, 
since clients cannot access server modules

Player folders
--In built maps , numbered folders are used to represent player unit spawns
--in game, depending on the player, and the order they are joined
-- the units are given to them, and are then tagged with Player.UserId

Each match is single use. and game replays are done by creating a new server via the lobby



Uses ModuleScripts named `Units`, `Objectives`, `AI`, and `Map` for seperation of 
concerns
]]

-- Services and Modules
local DataStoreService = game:GetService("DataStoreService")
local teleportData = DataStoreService:GetDataStore("TeleportData")
local events = game.ReplicatedStorage.Events
local players = game:GetService("Players")
local runService = game:GetService("RunService")

local units -- Handles Unit Creation
local objectives -- Win/lose conditions for player and ai enemy
local AI  -- AI logic for enemy units
local mapModule  -- Interacting with Mao
local SafeTeleport -- Safe Teleport module (basically a TeleportService wrapper, with better error handling)

-- Globals
local joinData = nil 
local testMapName = "Roadside Ambush" -- hardcoded Testing map

-- Built in Util: Shuffle table in-place

--why are soundtracks shuffled instead of cycled?
--cycling can make the soundtrack feel repetitive

local function shuffle(tbl)
	for i = #tbl, 2, -1 do
		local j = math.random(i)
		tbl[i], tbl[j] = tbl[j], tbl[i]
	end
	return tbl
end

-- inject a test map and mock joinData , if in Studio
if runService:IsStudio() then
	local mapInstance = workspace:FindFirstChild(testMapName)
	if mapInstance then
		mapInstance.Parent = game.ServerStorage.Maps
	end
	joinData = { Map = testMapName, WaitingPlayers = 1 }
end

-- Retrieve teleport and map data from the 1st player to join
if not joinData then
	players.PlayerAdded:Connect(function(plr)
        -- im using pcall for DataStore:GetAsync since TeleportService can throw 
        -- unpredictable errors.
        -- and pcall has error handling for this too
		local success, result = pcall(function()
			return teleportData:GetAsync(plr.UserId)
		end)

		if success and result then
			joinData = result
			teleportData:RemoveAsync(plr.UserId)
		end
	end)
end

-- Clean garbage from workspace
if not runService:IsRunMode() then
	workspace:FindFirstChild("Other"):Destroy()
end

-- Move dev unit models from workspace to ReplicatedStorage
for _, child in pairs(workspace.Units.Units:GetChildren()) do
	child.Parent = game.ReplicatedStorage.Units.Units
end
for _, child in pairs(workspace.Units.Skins:GetChildren()) do
	child.Parent = game.ReplicatedStorage.Units.Skins
end
-- After moving units into ReplicatedStorage, the workspace copy is destryoed
-- to make sure no duplicates exist
workspace.Units:Destroy()

-- Wait until teleport data (joinData) is available
-- task.wait is used instead of wait,because its less performance heavy, and updates 
--twice as fast
repeat task.wait() until joinData

-- Add selected map to workspace
--Maps are stored in ServerStorage instead of ReplicatedStorage for security.
--Maps contain information such as enemy spawns, and ai behaviour
--that players should not access
local map = game.ServerStorage.Maps:FindFirstChild(joinData.Map, true)
map:SetAttribute("Name", map.Name)
map.Name = "Map"
map.Parent = workspace


for _, team in pairs(map.Teams:GetChildren()) do
	team.Parent = game.Teams
end

--Modules are loaded later, because there are some dependecies that 
--need to be created first before
--initialising the Modules
task.spawn(function()
	units = require(script.Units) 
	objectives = require(script.Objectives)
	AI = require(script.AI) 
	mapModule = require(script.Map) 
	SafeTeleport = require(script.SafeTeleport)
end)

-- Waits until all players load in
repeat task.wait() until #players:GetPlayers() == joinData.WaitingPlayers

-- Vote tracker
--stored in DoubleConstrainedValue to enforce min/max bounds.
local deploymentVotes = Instance.new("DoubleConstrainedValue")
deploymentVotes.Name = "Votes"
deploymentVotes.MaxValue = joinData.WaitingPlayers
deploymentVotes.MinValue = 0
deploymentVotes.Parent = game.ServerStorage


events.Special.StartDeployment:FireAllClients()

-- if in studio, use a test loadout for 1 player
local loadout = nil
if runService:IsStudio() then
	loadout = { "Rifleman", "Tankette", "Light Tank" }
end

for _, folder in pairs(map.Deployment:GetChildren()) do
	local plr = players:GetPlayers()[tonumber(folder.Name)]
	for _, tile in pairs(folder:GetChildren()) do
        --MouseClick handles are connected here, instead of being centralised elsewhere. 
-- the code size is manageable, and avoids unnessecary centralisation.
		tile.ClickDetector.MouseClick:Connect(function(clickedPlr)
			if plr == clickedPlr then
				local selectedUnit = game.ReplicatedStorage.Functions.GetSelected:InvokeClient(plr)
				tile.Name = selectedUnit.Name
				tile.SurfaceGui.TextLabel.Text = selectedUnit.Name
				tile.SurfaceGui.Enabled = true
				tile.Parent = workspace.Map.Spawns[tostring(folder.Name)].Start
			end
		end)
	end
end


game.ReplicatedStorage.Functions.AddVote.OnServerInvoke = function(plr)
	deploymentVotes.Value += 1
	repeat task.wait() until deploymentVotes.Value == deploymentVotes.MaxValue
	return true
end

-- wait until all players vote to start
repeat task.wait() until deploymentVotes.Value == deploymentVotes.MaxValue

-- Game Starts
events.Special.GameStart:FireAllClients()

-- the map will have a playlist of soundtracks, plays through that
-- this is wrapped in a coroutine to run paralell with the script
-- if done inline, it would delay script execution
coroutine.wrap(function()
	if map:FindFirstChild("Soundtrack") then
		for _, sound in pairs(shuffle(map.Soundtrack:GetChildren())) do
			sound:Play()
			sound.Ended:Wait()
		end
	end
end)()

-- Uni Ownership
-- why use Numbered folders for player spawns?
--when designing maps, slots 1,2,3 are used to represent different players that join
-- Once the game starts, these folders are mapped to real Player.UserIds.
for _, unit in pairs(workspace.Units:GetChildren()) do
	local val = tonumber(unit.Configuration.Player.Value)
	if val and players:GetChildren()[val] then

		unit.Configuration.Player.Value = players:GetChildren()[val].Name
	else
		unit:Destroy()
	end
end

-- clean up spawn (where you can place units) visuals
for _, part in pairs(workspace.Map.Spawns:GetDescendants()) do
	if part:IsA("BasePart") then
		part.Transparency = 1
		local gui = part:FindFirstChildWhichIsA("SurfaceGui") or part:FindFirstChildWhichIsA("Decal")
		gui:Destroy()
	end
end

-- for spawning a group of units, to share a common behaviour, so that AI can understand which
-- units to move as a group
local function spawnTeam(teamName, team)
	for _, spawn in ipairs(team:GetChildren()) do
		if spawn:IsA("Folder") then
			units.Spawn(teamName, spawn, spawn)
		else
			units.Spawn(teamName, spawn.Name, spawn)
		end
	end
end

-- Scripted enemy/neutral/ally spawn logic
if workspace.Map.Spawns:FindFirstChild("Enemy") then
	spawnTeam(game.Teams.Enemy:FindFirstChildWhichIsA("Humanoid"), workspace.Map.Spawns.Enemy.Start)
end
if workspace.Map.Spawns:FindFirstChild("Neutral") then
	spawnTeam(nil, workspace.Map.Spawns.Neutral.Start)
end
if workspace.Map.Spawns:FindFirstChild("Ally") then
	spawnTeam(game.Teams.Players:FindFirstChildWhichIsA("Humanoid"), workspace.Map.Spawns.Ally.Start)
end

-- spawn players units
for index, plr in ipairs(players:GetPlayers()) do
	spawnTeam(plr, workspace.Map.Spawns[tostring(index)].Start)
end

-- Load plyaer objectives
for _, folder in pairs(game.ServerStorage.Objectives.Players:GetChildren()) do
	if folder.Name == "Start" then
		for _, obj in pairs(folder:GetChildren()) do
			objectives.Add(obj, game.Teams.Players)
		end
	end
end

task.wait(1)
events.MapEvent:FireAllClients(workspace.Map.Events.GameStart)

-- Start match timer
-- match time is stored as an IntValue in ServerStorage, not as a global variable
-- which allows the client to access this information
-- this wouldnt be possible if it was stored in the form
-- _G.MatchTime = 0
-- values are also synced with the clients automatically
local Time = Instance.new("IntValue", game.ServerStorage)
Time.Name = "Time"
coroutine.wrap(function()
	while task.wait(1) do
		Time.Value += 1
	end
end)()

-- Listen to map events
for _, mapEvent in pairs(workspace.Map.Events:GetChildren()) do
	local duration = mapEvent:GetAttribute("OnDuration")
	if duration then
		coroutine.wrap(function()
			repeat task.wait() until Time.Value == duration
			mapEvent:Fire()
		end)()
	end
	mapEvent.Event:Connect(function()
		events.MapEvent:FireAllClients(mapEvent)
	end)
end

-- Wait until game ends (determined by the Objectives module)
local gameEnd, win = false, false
game.ServerStorage.Events.GameEnd.Event:Connect(function(x)
	win = x
	gameEnd = true
end)
repeat task.wait() until gameEnd

-- Screen End for all players, display win/lose condition and reward if there is a win
for _, plr in pairs(players:GetPlayers()) do
	for _, frame in pairs(plr.PlayerGui.GameGui:GetChildren()) do
		if frame:IsA("Frame") then
			frame.Visible = false
		end
	end
	plr.PlayerGui.GameGui.EndScreen.Visible = true
	if not win then
		plr.PlayerGui.GameGui.EndScreen.Ending.Text = "DEFEAT"
		plr.PlayerGui.GameGui.EndScreen.Stats.Visible = false
	end
end

workspace.Audio.GUI.GameEnd:Play()
task.wait(1)

-- Countdown before teleporting players out
for _, plr in pairs(players:GetPlayers()) do
	coroutine.wrap(function()
		local countdown = plr.PlayerGui.GameGui.EndScreen.Countdown
		countdown.Visible = true
		for i = 10, 0, -1 do
			countdown.Text = "Teleporting back in..." .. i
			task.wait(1)
		end
	end)()
end

-- SafeTeleport Module is used

-- using coroutine.wrap for teleport keeps this seciton lighweight
-- using seperate threads in this instance would be overkill
coroutine.wrap(function()
    -- An extra few seconds to the countdown is added, to account for
    -- any network lag
	task.wait(14)
	local TARGET_PLACE_ID = 15418890445
	local options = Instance.new("TeleportOptions")
	options:SetTeleportData({ victory = true })
    --Safe teleport is used, because it has better error handling compared to the using the default TeleportService
	SafeTeleport(TARGET_PLACE_ID, players:GetPlayers(), options)
end)()
