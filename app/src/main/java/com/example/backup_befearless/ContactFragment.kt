package com.example.backup_befearless


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.nisrulz.sensey.Sensey
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.android.synthetic.main.custom_topbar.*
import kotlinx.android.synthetic.main.fragment_contact.*


/**
 * A simple [Fragment] subclass.
 */
class ContactFragment : Fragment(), ItemClickListener {

    private var data: User? = null
    lateinit var contactAdapter: ContactAdapter
    var db: FirebaseFirestore? = null
    lateinit var numbers: ArrayList<ContactModel>
    var docRef: DocumentReference? = null
    var realtimeWatcher: ListenerRegistration? = null


    override fun onDelete(position: Int) {
        numbers.removeAt(position)
        docRef?.get()
            ?.addOnSuccessListener {
                data = it.toObject(User::class.java)
                if (data != null) {
                    data!!.numbers = numbers
                    docRef!!.set(data!!)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        db = FirebaseFirestore.getInstance()
        Sensey.getInstance().init(context);


        return inflater.inflate(R.layout.fragment_contact, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvActionBarTitle.setText(getText(R.string.contacts))

        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser


        contactList.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
        }

        numbers = arrayListOf()


        data = User()

        docRef = db?.collection("users")?.document(currentUser?.uid.toString())


        docRef?.get()
            ?.addOnSuccessListener {
                data = it.toObject(User::class.java)
                if (data != null && data?.numbers != null) {
                    numbers = data!!.numbers as ArrayList<ContactModel>
                }
                contactAdapter = ContactAdapter(numbers, this)
                contactList?.adapter = contactAdapter

            }

        realtimeWatcher = docRef?.addSnapshotListener { snapshot, e ->
            if (e != null) {
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                data = snapshot.toObject(User::class.java)
                if (data != null && data?.numbers != null) {
                    numbers = data!!.numbers as ArrayList<ContactModel>
                }
                contactAdapter = ContactAdapter(numbers, this)
                contactList?.adapter = contactAdapter
            }
        }



        btnAdd.setOnClickListener {
            etAddNumber.setError(null)
            etName.setError(null)
            if (!etAddNumber.text.isNullOrEmpty() && !etName.text.isNullOrEmpty()) {
                docRef?.get()
                    ?.addOnSuccessListener {
                        data = it.toObject(User::class.java)
                        if (data != null) {
                            numbers.add(
                                ContactModel(
                                    number = etAddNumber.text.toString(),
                                    contactName = etName.text.toString().trim()
                                )
                            )
                            data!!.numbers = numbers
                            docRef!!.set(data!!)
                            etAddNumber.text!!.clear()
                            etName.text!!.clear()
                        }
                    }
            } else if (etName.text.isNullOrEmpty()) {
                etAddNumber.setError("Provide a valid number")
            } else if (etAddNumber.text.isNullOrEmpty()) {
                etName.setError("Provide a valid number")
            }
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        realtimeWatcher?.remove()
    }


}
