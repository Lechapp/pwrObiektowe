package pl.pwr.pogoda.elements

class ReplaceSign {

    companion object{

        fun removePolishSign(s: String):String{

            val original = arrayOf("Ą", "ą", "Ć", "ć", "Ę", "ę", "Ł", "ł", "Ń", "ń", "Ó", "ó", "Ś", "ś", "Ź", "ź", "Ż", "ż")

            val normalized = arrayOf("A", "a", "C", "c", "E", "e", "L", "l", "N", "n", "O", "o", "S", "s", "Z", "z", "Z", "z")

            val newString = s
            for(i in original.indices){
                newString.replace(original[i], normalized[i])
            }

            return newString

        }
    }
}