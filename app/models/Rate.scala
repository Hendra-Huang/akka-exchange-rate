package models

import play.api.libs.json.Json

case class Rate (
  usdeur: Double,
  usdidr: Double,
  usdmyr: Double,
  usdsgd: Double,
  usdjpy: Double,
  usdcny: Double
)

object Rate {
  implicit val format = Json.format[Rate]
}
