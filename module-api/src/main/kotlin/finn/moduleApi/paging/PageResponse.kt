package finn.moduleApi.paging

data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val hasNext: Boolean
) {
}