package com.example.composepokedex.presentation.pokemonlist.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.capitalize
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.composepokedex.data.model.PokedexListEntry
import com.example.composepokedex.repository.PokemonRepository
import com.example.composepokedex.util.Constants
import com.example.composepokedex.util.Constants.PAGE_SIZE
import com.example.composepokedex.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class PokemonListViewModel @Inject constructor(
    private val repository: PokemonRepository
) : ViewModel() {

    private var currentPage = 0

    var pokemonList = mutableStateOf<List<PokedexListEntry>>(listOf())
    var loadError = mutableStateOf("")
    var isLoading = mutableStateOf(false)
    var endReached = mutableStateOf(false)

    init {
        loadPokemonPaginated()
    }

    fun loadPokemonPaginated() {
        isLoading.value = true
        viewModelScope.launch {
            val result = repository.getPokemonList(PAGE_SIZE, currentPage * PAGE_SIZE)
            when(result){
                is Resource.Success -> {
                    endReached.value = currentPage * PAGE_SIZE >= result.data!!.count
                    val pokedexEntry = result.data.results.mapIndexed { index, entry ->
                        val number = if(entry.url.endsWith("/")) {
                            entry.url.dropLast(1).takeLastWhile { it.isDigit() }
                        } else {
                            entry.url.takeLastWhile { it.isDigit() }
                        }

                        val url = "\"https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${number}.png"
                        PokedexListEntry(
                            entry.name.capitalize(Locale.ROOT),
                            url,
                            number.toInt()
                        )
                    }
                    currentPage++

                    loadError.value = ""
                    isLoading.value = false
                    pokemonList.value += pokedexEntry
                }
                is Resource.Error -> {
                    isLoading.value = false

                }
            }
        }
    }

}