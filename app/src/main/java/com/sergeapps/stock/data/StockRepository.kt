package com.sergeapps.stock.data

import android.content.Context
import android.net.Uri
import com.sergeapps.stock.util.MultipartUtils
import com.sergeapps.stock.vm.ManufDto
import com.sergeapps.stock.vm.ManufUi
import java.util.logging.Filter


class StockRepository(
    private val api: StockApiService
) {
    suspend fun fetchManufacturers(
        nbItems: Int,
        pageNumber: Int
    ): List<ManufUi> {
        return api.getManufList(nbItems, pageNumber)
            .map { dto: ManufDto ->
                ManufUi(name = dto.description.orEmpty())
            }
            .filter { manuf: ManufUi ->
                manuf.name.isNotBlank()
            }
    }

    suspend fun loadItemsPage(
        page: Int,
        nbItems: Int
    ): List<ItemListDto> {
        return api.getItemList(
            pageNumber = page,
            orderBy = "description",
            nbItems = nbItems
        )
    }

    suspend fun loadItemsTotalPages(nbItems: Int, filter: String?): Int {
        val dto = api.getNbPagesItem(
            nbItems = nbItems,
            filter = filter
        )

        return dto.nbPages.toIntOrNull() ?: 1
    }

    suspend fun loadItemDetail(itemId: Int): ItemDetailDto {
        return api.getItemDetail(id = itemId)
    }

    // --- PHOTO ---

    suspend fun uploadPhoto(
        context: Context,
        itemId: Int,
        uri: Uri
    ): UploadPicResponseDto {
        val part = MultipartUtils.uriToMultipart(
            context = context,
            uri = uri,
            partName = "file" // le nom n'est pas important pour ton busboy, mais c'est plus clair
        )

        return api.uploadPic(
            id = itemId,
            file = part
        )
    }

    suspend fun deletePhoto(itemId: Int, pictureUrl: String): DeletePictureResponseDto {
        return api.deletePicture(
            id = itemId,
            url = pictureUrl
        )
    }

    suspend fun loadVendors(
        page: Int = 1,
        nbItems: Int = 10
    ): List<VendorRowDto> {
        return api.getVendorList(
            pageNumber = page,
            nbItems = nbItems
        )
    }

}

