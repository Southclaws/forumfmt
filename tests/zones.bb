[COLOR=#FF4700][SIZE=7][B]SA-MP Map Zones[/B][/SIZE][/COLOR]

[URL=https://github.com/kristoisberg/samp-map-zones][IMG]https://shields.southcla.ws/badge/sampctl-samp--map--zones-2f2f2f.svg?style=for-the-badge[/IMG][/URL]

This library does not bring anything gamechanging to the table, it’s created to stop a decade long era of bad practices regarding map zones. An array of ~350 zones dumped (or manually converted?) from the game has been around for such a long time, but in that time I’ve never seen a satisfactory API for them. Let’s look at an implementation from Emmet_’s South Central Roleplay.

[CODE]
[COLOR=DeepSkyBlue]stock[/COLOR] GetLocation(Float:fX, Float:fY, Float:fZ)
{
    [COLOR=Blue]enum[/COLOR] e_ZoneData
    {
        e_ZoneName[[COLOR=Purple]32[/COLOR] [COLOR=Blue]char[/COLOR]],
        Float:e_ZoneArea[[COLOR=Purple]6[/COLOR]]
    };
    [COLOR=Blue]new[/COLOR] [COLOR=DeepSkyBlue]const[/COLOR] g_arrZoneData[][e_ZoneData] =
    {
        [COLOR=Green]// ...[/COLOR]
    };
    [COLOR=Blue]new[/COLOR]
        name[[COLOR=Purple]32[/COLOR]] = [COLOR=Purple]"San Andreas"[/COLOR];

    [COLOR=Blue]for[/COLOR] ([COLOR=Blue]new[/COLOR] i = [COLOR=Purple]0[/COLOR]; i != sizeof(g_arrZoneData); i ++)
    {
        [COLOR=Blue]if[/COLOR] (
            (fX >= g_arrZoneData[i][e_ZoneArea][[COLOR=Purple]0[/COLOR]] && fX <= g_arrZoneData[i][e_ZoneArea][[COLOR=Purple]3[/COLOR]]) &&
            (fY >= g_arrZoneData[i][e_ZoneArea][[COLOR=Purple]1[/COLOR]] && fY <= g_arrZoneData[i][e_ZoneArea][[COLOR=Purple]4[/COLOR]]) &&
            (fZ >= g_arrZoneData[i][e_ZoneArea][[COLOR=Purple]2[/COLOR]] && fZ <= g_arrZoneData[i][e_ZoneArea][[COLOR=Purple]5[/COLOR]]))
        {
            strunpack(name, g_arrZoneData[i][e_ZoneName]);

            [COLOR=Blue]break[/COLOR];
        }
    }
    [COLOR=Blue]return[/COLOR] name;
}

[COLOR=DeepSkyBlue]stock[/COLOR] GetPlayerLocation(playerid)
{
    [COLOR=Blue]new[/COLOR]
        Float:fX,
        Float:fY,
        Float:fZ,
        string[[COLOR=Purple]32[/COLOR]],
        id = [COLOR=Purple]-1[/COLOR];

    [COLOR=Blue]if[/COLOR] ((id = House_Inside(playerid)) != [COLOR=Purple]-1[/COLOR])
    {
        fX = HouseData[id][housePos][[COLOR=Purple]0[/COLOR]];
        fY = HouseData[id][housePos][[COLOR=Purple]1[/COLOR]];
        fZ = HouseData[id][housePos][[COLOR=Purple]2[/COLOR]];
    }
    [COLOR=Green]// ...[/COLOR]
    [COLOR=Blue]else[/COLOR] GetPlayerPos(playerid, fX, fY, fZ);

    format(string, [COLOR=Purple]32[/COLOR], GetLocation(fX, fY, fZ));
    [COLOR=Blue]return[/COLOR] string;
}
[/CODE]

[IMG]https://i.imgur.com/cyUdlu4.png[/IMG]

If you didn’t get the reference, you should probably check out [URL=https://github.com/sampctl/pawn-array-return-bug]this repository[/URL]. [FONT=courier new]GetPlayerLocation[/FONT] most likely uses [FONT=courier new]format[/FONT] to prevent this bug from occurring, but the risk is still there and arrays should never be returned in PAWN. Let’s take a look at another implementation that even I used a long time ago.

[CODE]
[COLOR=DeepSkyBlue]stock[/COLOR] GetPointZone(Float:x, Float:y, Float:z, zone[] = [COLOR=Purple]"San Andreas"[/COLOR], len = sizeof(zone))
{
    [COLOR=Blue]for[/COLOR] ([COLOR=Blue]new[/COLOR] i, j = sizeof(Zones); i < j; i++)
    {
        [COLOR=Blue]if[/COLOR] (x >= Zones[i][zArea][[COLOR=Purple]0[/COLOR]] && x <= Zones[i][zArea][[COLOR=Purple]3[/COLOR]] && y >= Zones[i][zArea][[COLOR=Purple]1[/COLOR]] && y <= Zones[i][zArea][[COLOR=Purple]4[/COLOR]] && z >= Zones[i][zArea][[COLOR=Purple]2[/COLOR]] && z <= Zones[i][zArea][[COLOR=Purple]5[/COLOR]])
        {
            strunpack(zone, Zones[i][zName], len);
            [COLOR=Blue]return[/COLOR] [COLOR=Purple]1[/COLOR];
        }
    }
    [COLOR=Blue]return[/COLOR] [COLOR=Purple]1[/COLOR];
}

[COLOR=DeepSkyBlue]stock[/COLOR] GetPlayerZone(playerid, zone[], len = sizeof(zone))
{
    [COLOR=Blue]new[/COLOR] Float:pos[[COLOR=Purple]3[/COLOR]];
    GetPlayerPos(playerid, pos[[COLOR=Purple]0[/COLOR]], pos[[COLOR=Purple]1[/COLOR]], pos[[COLOR=Purple]2[/COLOR]]);

    [COLOR=Blue]for[/COLOR] ([COLOR=Blue]new[/COLOR] i, j = sizeof(Zones); i < j; i++)
    {
        [COLOR=Blue]if[/COLOR] (x >= Zones[i][zArea][[COLOR=Purple]0[/COLOR]] && x <= Zones[i][zArea][[COLOR=Purple]3[/COLOR]] && y >= Zones[i][zArea][[COLOR=Purple]1[/COLOR]] && y <= Zones[i][zArea][[COLOR=Purple]4[/COLOR]] && z >= Zones[i][zArea][[COLOR=Purple]2[/COLOR]] && z <= Zones[i][zArea][[COLOR=Purple]5[/COLOR]])
        {
            strunpack(zone, Zones[i][zName], len);
            [COLOR=Blue]return[/COLOR] [COLOR=Purple]1[/COLOR];
        }
    }
    [COLOR=Blue]return[/COLOR] [COLOR=Purple]1[/COLOR];
}
[/CODE]

First of all, what do we see? A lot of code repetition. That’s easy to fix in this case, but what if we also needed either the min/max position of the zone? We’d have to loop through the zones again or take a different approach. Which approach does this library take? Functions like [FONT=courier new]GetMapZoneAtPoint[/FONT] and [FONT=courier new]GetPlayerMapZone[/FONT] do not return the name of the zone, they return an identificator of it. The name or positions of the zone must be fetched using another function. In addition to that, I rebuilt the array of zones myself since the one used basically everywhere seems to be faulty according to [URL=https://forum.sa-mp.com/showpost.php?p=4050745&postcount=7]this post[/URL].

[COLOR=RoyalBlue][SIZE=6][B]Installation[/B][/SIZE][/COLOR]

Simply install to your project:

[CODE]
sampctl package install kristoisberg/samp-map-zones
[/CODE]

Include in your code and begin using the library:

[CODE]
[COLOR=Blue]#include <map-zones>[/COLOR]
[/CODE]

[COLOR=RoyalBlue][SIZE=6][B]Usage[/B][/SIZE][/COLOR]

[COLOR=DeepSkyBlue][SIZE=5][B]Constants[/B][/SIZE][/COLOR]

[LIST]
[*][FONT=courier new]INVALID_MAP_ZONE_ID = MapZone:-1[/FONT]

[LIST]
[*]The return value of several functions when no map zone was matching the
criteria.
[/LIST]
[*][FONT=courier new]MAX_MAP_ZONE_NAME = 27[/FONT]

[LIST]
[*]The length of the longest map zone name including the null character.
[/LIST]
[*][FONT=courier new]MAX_MAP_ZONE_AREAS = 13[/FONT]

[LIST]
[*]The most areas associated with a map zone.
[/LIST]
[/LIST]

[COLOR=DeepSkyBlue][SIZE=5][B]Functions[/B][/SIZE][/COLOR]

[LIST]
[*][FONT=courier new]MapZone:GetMapZoneAtPoint(Float:x, Float:y, Float:z)[/FONT]

[LIST]
[*]Returns the ID of the map zone the point is in or [FONT=courier new]INVALID_MAP_ZONE_ID[/FONT] if
it isn’t in any. Alias: [FONT=courier new]GetMapZoneAtPoint3D[/FONT].
[/LIST]
[*][FONT=courier new]MapZone:GetPlayerMapZone(playerid)[/FONT]

[LIST]
[*]Returns the ID of the map zone the player is in or [FONT=courier new]INVALID_MAP_ZONE_ID[/FONT] if
it isn’t in any. Alias: [FONT=courier new]GetPlayerMapZone3D[/FONT].
[/LIST]
[*][FONT=courier new]MapZone:GetVehicleMapZone(vehicleid)[/FONT]

[LIST]
[*]Returns the ID of the map zone the vehicle is in or [FONT=courier new]INVALID_MAP_ZONE_ID[/FONT] if
it isn’t in any. Alias: [FONT=courier new]GetVehicleMapZone3D[/FONT].
[/LIST]
[*][FONT=courier new]MapZone:GetMapZoneAtPoint2D(Float:x, Float:y)[/FONT]

[LIST]
[*]Returns the ID of the map zone the point is in or [FONT=courier new]INVALID_MAP_ZONE_ID[/FONT] if
it isn’t in any. Does not check the Z-coordinate.
[/LIST]
[*][FONT=courier new]MapZone:GetPlayerMapZone2D(playerid)[/FONT]

[LIST]
[*]Returns the ID of the map zone the player is in or [FONT=courier new]INVALID_MAP_ZONE_ID[/FONT] if
it isn’t in any. Does not check the Z-coordinate.
[/LIST]
[*][FONT=courier new]MapZone:GetVehicleMapZone2D(vehicleid)[/FONT]

[LIST]
[*]Returns the ID of the map zone the vehicle is in or [FONT=courier new]INVALID_MAP_ZONE_ID[/FONT] if
it isn’t in any. Does not check the Z-coordinate.
[/LIST]
[*][FONT=courier new]bool:IsValidMapZone(MapZone:id)[/FONT]

[LIST]
[*]Returns [FONT=courier new]true[/FONT] or [FONT=courier new]false[/FONT] depending on if the map zone is valid or not.
[/LIST]
[*][FONT=courier new]bool:GetMapZoneName(MapZone:id, name[], size = sizeof(name))[/FONT]

[LIST]
[*]Retrieves the name of the map zone. Returns [FONT=courier new]true[/FONT] or [FONT=courier new]false[/FONT] depending on
if the map zone is valid or not.
[/LIST]
[*][FONT=courier new]bool:GetMapZoneSoundID(MapZone:id, &soundid)[/FONT]

[LIST]
[*]Retrieves the sound ID of the map zone. Returns [FONT=courier new]true[/FONT] or [FONT=courier new]false[/FONT] depending
on if the map zone is valid or not.
[/LIST]
[*][FONT=courier new]bool:GetMapZoneAreaCount(MapZone:id, &count)[/FONT]

[LIST]
[*]Retrieves the count of areas associated with the map zone. Returns [FONT=courier new]true[/FONT] or
[FONT=courier new]false[/FONT] depending on if the map zone is valid or not.
[/LIST]
[*][FONT=courier new]GetMapZoneAreaPos(MapZone:id, &Float:minX = 0.0, &Float:minY = 0.0, &Float:minZ = 0.0, &Float:maxX = 0.0, &Float:maxY = 0.0, &Float:maxZ = 0.0, start = 0)[/FONT]

[LIST]
[*]Retrieves the coordinates of an area associated with the map zone. Returns
the array index for the area or [FONT=courier new]-1[/FONT] if none were found. See the usage in
in the examples section.
[/LIST]
[*][FONT=courier new]GetMapZoneCount()[/FONT]

[LIST]
[*]Returns the count of map zones in the array. Could be used for iteration
purposes.
[/LIST]
[/LIST]

[COLOR=RoyalBlue][SIZE=6][B]Examples[/B][/SIZE][/COLOR]

[COLOR=DeepSkyBlue][SIZE=5][B]Retrieving the location of a player[/B][/SIZE][/COLOR]

[CODE]
CMD:whereami(playerid) {
    [COLOR=Blue]new[/COLOR] MapZone:zone = GetPlayerMapZone(playerid);

    [COLOR=Blue]if[/COLOR] (zone == INVALID_MAP_ZONE_ID) {
        [COLOR=Blue]return[/COLOR] SendClientMessage(playerid, [COLOR=Purple][COLOR=Purple]0[/COLOR]xFFFFFFFF[/COLOR], [COLOR=Purple]"probably in the ocean, mate"[/COLOR]);
    }

    [COLOR=Blue]new[/COLOR] name[MAX_MAP_ZONE_NAME], soundid;
    GetMapZoneName(zone, name);
    GetMapZoneSoundID(zone, soundid);

    [COLOR=Blue]new[/COLOR] string[[COLOR=Purple]128[/COLOR]];
    format(string, sizeof(string), [COLOR=Purple]"you are in %s"[/COLOR], name);

    SendClientMessage(playerid, [COLOR=Purple][COLOR=Purple]0[/COLOR]xFFFFFFFF[/COLOR], string);
    PlayerPlaySound(playerid, soundid, [COLOR=Purple]0[/COLOR].[COLOR=Purple]0[/COLOR], [COLOR=Purple]0[/COLOR].[COLOR=Purple]0[/COLOR], [COLOR=Purple]0[/COLOR].[COLOR=Purple]0[/COLOR]);
    [COLOR=Blue]return[/COLOR] [COLOR=Purple]1[/COLOR];
}
[/CODE]

[COLOR=DeepSkyBlue][SIZE=5][B]Iterating through areas associated with a map zone[/B][/SIZE][/COLOR]

[CODE]
[COLOR=Blue]new[/COLOR] zone = ZONE_RICHMAN, index = [COLOR=Purple]-1[/COLOR], Float:minX, Float:minY, Float:minZ, Float:maxX, Float:maxY, Float:maxZ;

[COLOR=Blue]while[/COLOR] ((index = GetMapZoneAreaPos(zone, minX, minY, minZ, maxX, maxY, maxZ, index + [COLOR=Purple]1[/COLOR]) != [COLOR=Purple]-1[/COLOR]) {
    printf([COLOR=Purple]"%f %f %f %f %f %f"[/COLOR], minX, minY, minZ, maxX, maxY, maxZ);
}
[/CODE]

[COLOR=DeepSkyBlue][SIZE=5][B]Extending[/B][/SIZE][/COLOR]

[CODE]
[COLOR=DeepSkyBlue]stock[/COLOR] MapZone:GetPlayerOutsideMapZone(playerid) {
    [COLOR=Blue]new[/COLOR] House:houseid = GetPlayerHouseID(playerid), Float:x, Float:y, Float:z;

    [COLOR=Blue]if[/COLOR] (houseid != INVALID_HOUSE_ID) { [COLOR=Green]// if the player is inside a house, get the exterior location of the house[/COLOR]
        GetHouseExteriorPos(houseid, x, y, z);
    } [COLOR=Blue]else[/COLOR] [COLOR=Blue]if[/COLOR] (!GetPlayerPos(playerid, x, y, z)) { [COLOR=Green]// the player isn't connected, presuming that GetPlayerHouseID returns INVALID_HOUSE_ID in that case [/COLOR]
        [COLOR=Blue]return[/COLOR] INVALID_MAP_ZONE_ID;
    }

    [COLOR=Blue]return[/COLOR] GetMapZoneAtPoint(x, y, z);
}
[/CODE]

[COLOR=RoyalBlue][SIZE=6][B]Testing[/B][/SIZE][/COLOR]

To test, simply run the package:

[CODE]
sampctl package run
[/CODE]

