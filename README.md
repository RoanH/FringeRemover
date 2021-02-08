# Fringe Remover ![](https://img.shields.io/github/release/RoanH/FringeRemover.svg) ![](https://img.shields.io/github/downloads/RoanH/FringeRemover/total.svg)
Program to make images not get weird white fringes when rendered in osu!. Might work as well for other games or OpenGL applications that render images at different sizes.

[Jump directly to downloads](#downloads)

# Forum post
A little while ago I was asked by [RockRoller](https://osu.ppy.sh/users/8388854) to write a program to address a common skinning issue where a white fringe appears around skin elements. If you make your own osu! skins and this issue sounds familiar then this program might be of use to you. I've also asked RockRoller to post a more detailed comment to this thread explaining why and when you might want to use this program.

The program looks like this:<br>
![GUI](https://i.imgur.com/AvLVcFa.png)

The program can either process individual files or you can process a whole folder at the same time. Furthermore, although I've done extensive testing I recommend that you always make a backup before running the program on your skin, just in case something goes wrong. It's also worth noting that only PNG files will be processed by this program.

To further highly exactly what this program does I've put together a simple example. The image is just a black circle with no anti-aliasing we'll use it as a mod icon.    
![Original](https://i.imgur.com/ZcqOnst.png)

If we now load our skin in osu! we will see the following result on the mod selection screen:    
![Result](https://i.imgur.com/1NcQ7g4.png)

Even though there was no white at all in the original image, we have white fringes around the circle. If we now run FringeRemover on this image and check again we end up with the following, expected result:    
![Fixed](https://i.imgur.com/M3ThzDd.png)

I hope that this program is useful to some of you :)<br>
If you find any bugs feel free to report them.

## More detailed explanation from RockRoller
Commonly when skinning you will notice how an image may be surrounded by a slight white border aka fringe that you didn't put there. These are just not very pleasant to look at and are a pain to deal with.

These happen because the transparent parts of your image are actually white at 100% transparency (255, 255, 255, 0). (Small disclaimer: I do not know the exact technicalities, but how to solve them and roughly why they happen.) 

In rendering the client averages neighboring pixels for scaling/anti aliasing and it does not ignore pixels with alpha = 0, therefore averaging your coloured pixel and fully transparent white, leading to a mixture of the them, which usually ends with lightening your colour and therefore producing a fringe. (this is also why its not noticeable on bright/white elements)

The fix to this was to set all alpha = 0 pixels to black, which can be done easily, however gets tedious when you need to do it for dozen or hundreds of files. This is where this program comes in. It does this for you. (technically it does it even better and should clamp the image, but that probably doesn't matter to you) 

Just make a copy of your skins (its always good to make a backup before potentially messing up your skin with some mass edit), select the skin, select the same folder for the output, check "Overwrite existing files" and hit start, once its done all white fringes on your skin should be done. 

## Downloads
_Requires Java 8 or higher_    
- [Windows executable](https://github.com/RoanH/FringeRemover/releases/download/v1.0/FringeRemover-v1.0.exe)<br>
- [Runnable Java Archive](https://github.com/RoanH/FringeRemover/releases/download/v1.0/FringeRemover-v1.0.jar)

All releases: [releases](https://github.com/RoanH/FringeRemover/releases)<br>
GitHub repository: [repository](https://github.com/RoanH/FringeRemover)<br>
Forum post: [post]()

Program icon by: [RockRoller](https://osu.ppy.sh/users/8388854)

## History
Project development started: 18th of January, 2021.