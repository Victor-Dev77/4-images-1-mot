package fr.esgi.app_4_images_1_word.controllers

const val EMPTY_STRING = " "
const val EMPTY_CHAR = ' '
const val DEFAULT_WORD_LETTER_BUTTON = "a"
val STRING_CHARACTERS = ('a'..'z').toList().toTypedArray()
const val PRICE_BONUS_ADD_LETTER = 80
const val PRICE_BONUS_DELETE_LETTER = 60
const val PRICE_WIN_LEVEL = 100
const val START_LEVEL = 1
const val START_NB_COIN = 400

const val ALERT_MISSING_COIN = "Pas assez de pièces !"
const val ALERT_MANY_LETTERS = "Enlever une lettre pour utiliser le bonus"
const val ALERT_EMPTY_WORD = "Toutes les lettres sont supprimées"
const val ALERT_AUTH_FAILED = "Authentication failed."
const val ALERT_DATA_ERROR = "Erreur loading data"

const val WORD_FIND = "MOT TROUVE !"
const val WORD_ERROR = "MOT ERRONNE..."
const val FINISH_GAME = "JEU FINI ! BRAVO"

const val COLLECTION_USER = "users"
const val USER_COL_ID = "id"
const val USER_COL_PSEUDO = "pseudo"
const val USER_COL_NBCOIN = "nbCoin"
const val USER_COL_ACTUALLEVEL = "actualLevel"

const val COLLECTION_LEVEL = "levels"
const val LEVEL_COL_LEVELNUMBER = "levelNumber"
const val LEVEL_COL_IMAGE = "image"
const val LEVEL_COL_WORD = "word"
const val LEVEL_COL_DIFFICULTY = "difficulty"