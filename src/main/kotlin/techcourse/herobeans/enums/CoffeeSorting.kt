package techcourse.herobeans.enums

import org.springframework.data.domain.Sort

enum class CoffeeSorting {
    ID_ASC,
    ID_DESC,
    NAME_ASC,
    NAME_DESC,
}

fun toSpringSort(sort: CoffeeSorting?): Sort {
    val s = sort ?: CoffeeSorting.NAME_ASC
    val base =
        when (s) {
            CoffeeSorting.ID_ASC -> Sort.by("id").ascending()
            CoffeeSorting.ID_DESC -> Sort.by("id").descending()
            CoffeeSorting.NAME_ASC -> Sort.by("name").ascending()
            CoffeeSorting.NAME_DESC -> Sort.by("name").descending()
        }
    return base.and(Sort.by("id"))
}
