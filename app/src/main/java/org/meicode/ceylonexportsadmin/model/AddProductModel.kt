package org.meicode.ceylonexportsadmin.model

class AddProductModel : java.io.Serializable {
    var productName: String? = ""
    var productDescription: String? = ""
    var productCoverImg: String? = ""
    var productCategory: String? = ""
    var productId: String? = ""
    var productMrp: String? = ""
    var productSp: String? = ""
    var productImages: ArrayList<String> = ArrayList()
}

