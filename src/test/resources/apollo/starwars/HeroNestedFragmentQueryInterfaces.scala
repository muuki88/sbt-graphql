trait CharacterFriends { def name: Option[String] }
trait CharacterInfo {
  def name: Option[String]
  def friends: Option[List[Option[CharacterFriends]]]
}