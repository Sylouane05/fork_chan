package fr.fork_chan.models

data class Post(
    var postId: String? = null,        // Identifiant unique du post
    var userId: String? = null,        // Identifiant de l'utilisateur qui a créé le post
    var description: String? = null,   // Contenu textuel du post
    var date: String? = null,          // Date du post, format "jour/mois/année heure:minute"
    var likeCount: Int = 0,            // Nombre de likes
    var imageUrls: List<String>? = null // Liste d'URLs des images associées au post (peut être vide ou null)
)
