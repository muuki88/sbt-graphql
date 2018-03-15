package com.example.client

import com.example.client.api.MultiQueryApi
import MultiQueryApi.HeroAndNestedFriends.Hero

object Main {
  def main(args: Array[String]): Unit = {
    println(MultiQueryApi.Episode.JEDI)
    println(Hero.Friends.Friends.Friends.Friends(name = Some("Far out friend")))
  }
}
