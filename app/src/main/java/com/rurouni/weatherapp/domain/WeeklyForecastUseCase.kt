package com.rurouni.weatherapp.domain

import com.rurouni.weatherapp.data.repository.WeatherRepository
import com.rurouni.weatherapp.data.source.remote.model.ForecastWeather
import com.rurouni.weatherapp.data.source.remote.model.Forecastday
import com.rurouni.weatherapp.data.source.remote.model.HistoryWeather
import com.rurouni.weatherapp.utils.DateOps
import com.rurouni.weatherapp.utils.NetWorkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class WeeklyForecastUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository
) {
    operator fun invoke(location : String, language : String, days: Int): Flow<NetWorkResult<ForecastWeather>> = flow {
        val yesterday = DateOps.getSystemDate(-1)
        val forecastDayLimit = 3
        val forecastFlow = weatherRepository.getRemoteForecast(location, language, forecastDayLimit.toString(), null)
        val historicForecastFlow = weatherRepository.getRemoteHistoricForecast(location, language, yesterday)

        var currentForecast: ForecastWeather? = null
        var historicForecast: HistoryWeather? = null

        forecastFlow.collect { result ->
            if (result is NetWorkResult.Success) {
                currentForecast = result.data
            }
        }

        historicForecastFlow.collect { result ->
            if (result is NetWorkResult.Success) {
                historicForecast = result.data
            }
        }

        val newList = arrayListOf<Forecastday>()

        historicForecast?.let { history ->
            newList.add(history.forecast.forecastday.first())
        }

        currentForecast?.let { forecast ->
            forecast.forecast.forecastday.forEach {
                newList.add(it)
            }

            for (i in (forecastDayLimit)..6) {
                val dt = DateOps.getSystemDate(i)

                val request = weatherRepository.getRemoteForecast(location, language, days.toString(), dt)

                request.collect { result ->
                    if (result is NetWorkResult.Success) {
                        result.data?.let {
                            newList.add(it.forecast.forecastday.first())
                        }
                    }
                }
            }

            forecast.forecast.forecastday = newList
            emit(NetWorkResult.Success(forecast))
        }
    }
}
