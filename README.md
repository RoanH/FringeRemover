# Fringe Remover ![](https://img.shields.io/github/release/RoanH/FringeRemover.svg) ![](https://img.shields.io/github/downloads/RoanH/FringeRemover/total.svg)
Program to make images not get weird white fringes when rendered in osu!. Might work as well for other games or OpenGL applications that render images at different sizes.

# Forum post
A little while ago I was asked by [RockRoller](https://osu.ppy.sh/users/8388854) to write a program to adds a common skinning issue where a white fringe appears around certain skin elements. If you make your own osu! skins and this issue sounds familiar then this program might be of use to you. I've also asked RockRoller to post a more detailed comment to this thread explaining why and when you might want to use this program.

The program looks like this:<br>
![GUI](https://i.imgur.com/AvLVcFa.png)

The program can either process individual files or you can process a whole folder at the same time. Furthermore, although I've done extensive testing I recommend that you always make a backup before running the program on your skin, just in case something goes wrong.

To further highly exactly what this program does I've put together a simple example. The image is just a black circle with no antialising we'll use it as a mod icon.    
![Original](https://i.imgur.com/ZcqOnst.png)

If we now load our skin in osu! we will see the following result on the mod selection screen:    
![Result](https://i.imgur.com/1NcQ7g4.png)

Even though there was no white at all in the original image, we have white fringes around the circle. If we now run FringeRemover on this image and check again we end up with the following, expected result:    
![Fixed](https://i.imgur.com/M3ThzDd.png)

I hope that this program is useful to some of you :)<br>
If you find any bugs feel free to report them.

## More detailed explanation from RockRoller
...

## Downloads
_Requires Java 8 or higher_    
- [Windows executable](https://github.com/RoanH/FringeRemover/releases/download/v1.0/FringeRemover-v1.0.exe)<br>
- [Runnable Java Archive](https://github.com/RoanH/FringeRemover/releases/download/v1.0/FringeRemover-v1.0.jar)

All releases: [releases](https://github.com/RoanH/FringeRemover/releases)<br>
GitHub repository: [repository](https://github.com/RoanH/FringeRemover)<br>
Forum post: [post]()

## History
Project development started: 18th of January, 2021.