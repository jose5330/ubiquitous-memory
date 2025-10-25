warn(script.Name .. " loaded")
local DataStoreService = game:GetService("DataStoreService")
local teleportData = DataStoreService:GetDataStore("TeleportData")
local events = game.ReplicatedStorage.Events
local players = game:GetService("Players")
local runService = game:GetService("RunService")
local units = require(script.Units) -- Handles Unit Creation
local objectives = require(script.Objectives) -- Win/lose conditions for player and ai enemy
local AI = require(script.AI) -- AI logic for enemy units
local mapModule = require(script.Map) -- Interacting with Mao
local SafeTeleport = require(script.SafeTeleport) -- Safe Teleport module (basically a TeleportService wrapper, with better error handling)
local joinData = nil
local testMapName = "Roadside Ambush" -- Testing map
local function shuffle(tbl)
    for i = #tbl, 2, -1 do
        local j = math.random(i)
        tbl[i], tbl[j] = tbl[j], tbl[i]
    end
    return tbl
end
if runService:IsStudio() then
    local mapInstance = workspace:FindFirstChild(testMapName)
    if mapInstance then
        mapInstance.Parent = game.ServerStorage.Maps
    end
    joinData = { Map = testMapName, WaitingPlayers = 1 }
end
if not joinData then
    players.PlayerAdded:Connect(function(plr)
        local success, result = pcall(function()
            return teleportData:GetAsync(plr.UserId)
        end)
        if success and result then
            joinData = result
            teleportData:RemoveAsync(plr.UserId)
        end
    end)
end
if not runService:IsRunMode() then
    workspace:FindFirstChild("Other")?.Destroy(workspace.Other)
end
for _, child in pairs(workspace.Units.Units:GetChildren()) do
    child.Parent = game.ReplicatedStorage.Units.Units
end
for _, child in pairs(workspace.Units.Skins:GetChildren()) do
    child.Parent = game.ReplicatedStorage.Units.Skins
end
workspace.Units:Destroy()
repeat task.wait() until joinData
local map = game.ServerStorage.Maps:FindFirstChild(joinData.Map, true)
map:SetAttribute("Name", map.Name)
map.Name = "Map"
map.Parent = workspace
for _, team in pairs(map.Teams:GetChildren()) do
    team.Parent = game.Teams
end
repeat task.wait() until #players:GetPlayers() == joinData.WaitingPlayers
local deploymentVotes = Instance.new("DoubleConstrainedValue")
deploymentVotes.Name = "Votes"
deploymentVotes.MaxValue = joinData.WaitingPlayers
deploymentVotes.MinValue = 0
deploymentVotes.Parent = game.ServerStorage
events.Special.StartDeployment:FireAllClients()
local loadout = nil
if runService:IsStudio() then
    loadout = { "Rifleman", "Tankette", "Light Tank" }
end
for _, folder in pairs(map.Deployment:GetChildren()) do
    local plr = players:GetPlayers()[tonumber(folder.Name)]
    for _, tile in pairs(folder:GetChildren()) do
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
repeat task.wait() until deploymentVotes.Value == deploymentVotes.MaxValue
events.Special.GameStart:FireAllClients()
coroutine.wrap(function()
    if map:FindFirstChild("Soundtrack") then
        for _, sound in pairs(shuffle(map.Soundtrack:GetChildren())) do
            sound:Play()
            sound.Ended:Wait()
        end
    end
end)()
for _, unit in pairs(workspace.Units:GetChildren()) do
    local val = tonumber(unit.Configuration.Player.Value)
    if val and players:GetChildren()[val] then
        unit.Configuration.Player.Value = players:GetChildren()[val].Name
    else
        unit:Destroy()
    end
end
for _, part in pairs(workspace.Map.Spawns:GetDescendants()) do
    if part:IsA("BasePart") then
        part.Transparency = 1
        local gui = part:FindFirstChildWhichIsA("SurfaceGui") or part:FindFirstChildWhichIsA("Decal")
        gui?.Destroy(gui)
    end
end
local function spawnTeam(teamName, team)
    for _, spawn in ipairs(team:GetChildren()) do
        if spawn:IsA("Folder") then
            units.Spawn(teamName, spawn, spawn)
        else
            units.Spawn(teamName, spawn.Name, spawn)
        end
    end
end
if workspace.Map.Spawns:FindFirstChild("Enemy") then
    spawnTeam(game.Teams.Enemy:FindFirstChildWhichIsA("Humanoid"), workspace.Map.Spawns.Enemy.Start)
end
if workspace.Map.Spawns:FindFirstChild("Neutral") then
    spawnTeam(nil, workspace.Map.Spawns.Neutral.Start)
end
if workspace.Map.Spawns:FindFirstChild("Ally") then
    spawnTeam(game.Teams.Players:FindFirstChildWhichIsA("Humanoid"), workspace.Map.Spawns.Ally.Start)
end
for index, plr in ipairs(players:GetPlayers()) do
    spawnTeam(plr, workspace.Map.Spawns[tostring(index)].Start)
end
for _, folder in pairs(game.ServerStorage.Objectives.Players:GetChildren()) do
    if folder.Name == "Start" then
        for _, obj in pairs(folder:GetChildren()) do
            objectives.Add(obj, game.Teams.Players)
        end
    end
end
 
task.wait(1)
events.MapEvent:FireAllClients(workspace.Map.Events.GameStart)
local Time = Instance.new("IntValue", game.ServerStorage)
Time.Name = "Time"
coroutine.wrap(function()
    while task.wait(1) do
        Time.Value += 1
    end
end)()
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
local gameEnd, win = false, false
game.ServerStorage.Events.GameEnd.Event:Connect(function(x)
    win = x
    gameEnd = true
end)
repeat task.wait() until gameEnd
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
coroutine.wrap(function()
    task.wait(14)
    local TARGET_PLACE_ID = 15418890445
    local options = Instance.new("TeleportOptions")
    options:SetTeleportData({ victory = true })
    SafeTeleport(TARGET_PLACE_ID, players:GetPlayers(), options)
end)()
