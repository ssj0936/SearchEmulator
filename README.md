<img src="https://github.com/ssj0936/SearchEmulator/assets/3841546/b8ce9be1-aadc-450c-8fc1-747ddcd088e8" width="270" height="612">

# SearchEmulator(project still ongoing)

A path search emulator inspired by my failed Google interview experience. Implementation of MVI architecture using Android compose, Flow, Coroutine, Hilt.

## How to play it :
This project is like those Leetcode graph problems, finding a path from a given point. 
* You start from **green** block and the destination is **yellow** block. 
* You can draw some **barrier** to preventing stepping into these blocks.
* You can choose some differents travelsal/searching algo. to see how they work and the animated process.
* You can pause, restart, stop, event speed up / slow down the speed of process animation at anytime.


## Some detail:
* Figure out correct recompose scope and reduce recompose count : [關於Recompose的注意事項](https://ssj0936.medium.com/%E9%97%9C%E6%96%BCrecompose%E7%9A%84%E6%B3%A8%E6%84%8F%E4%BA%8B%E9%A0%85-128918014d90)

## To-do
* ~~Random generated maze~~ done
* Landscape layout
* ~~Algo. explain section (maybe present in side drawer)~~ done
* More Algo options
* 2 start points and 2 end points (exactly what I be asked in interview)
* ~~Beautify UI (Better Dark theme UI)~~ done


## Reference
[Graph traversal - Wikipedia](https://en.wikipedia.org/wiki/Graph_traversal) <br>
[Path (graph theory) - Wikipedia](https://en.wikipedia.org/wiki/Path_(graph_theory)) <br>
[Maze generation algorithm](https://en.wikipedia.org/wiki/Maze_generation_algorithm)

### Some Demo video
https://github.com/ssj0936/SearchEmulator/assets/3841546/68407e5c-069b-4a75-9942-197b0eb0096c

https://github.com/ssj0936/SearchEmulator/assets/3841546/3b9600f3-6f9c-43b5-a3d6-54de0e56081e

https://github.com/ssj0936/SearchEmulator/assets/3841546/2688346f-e1ad-40dd-b52b-ec9ec4e189a8
