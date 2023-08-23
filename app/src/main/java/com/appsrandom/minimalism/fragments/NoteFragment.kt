package com.appsrandom.minimalism.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.appsrandom.minimalism.R
import com.appsrandom.minimalism.activities.CreateOrEditNoteActivity
import com.appsrandom.minimalism.adapters.RVFoldersAdapter
import com.appsrandom.minimalism.adapters.RVNotesAdapter
import com.appsrandom.minimalism.databinding.FragmentNoteBinding
import com.appsrandom.minimalism.models.Folder
import com.appsrandom.minimalism.utils.SwipeToDelete
import com.appsrandom.minimalism.utils.hideKeyboard
import com.appsrandom.minimalism.viewModel.NoteViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import java.util.concurrent.TimeUnit

class NoteFragment : Fragment() {

    private lateinit var binding: FragmentNoteBinding
    private val noteViewModel: NoteViewModel by activityViewModels()
    private lateinit var rvNotesAdapter: RVNotesAdapter
    private lateinit var rvFoldersAdapter: RVFoldersAdapter
    private lateinit var sharedPreferencesView: SharedPreferences
    private lateinit var sharedPreferencesSort: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireView().hideKeyboard()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentNoteBinding.inflate(layoutInflater, container, false)

        sharedPreferencesView = activity?.getSharedPreferences("sharedPrefsView", 0) as SharedPreferences
        sharedPreferencesSort = activity?.getSharedPreferences("sharedPrefsSort", 0) as SharedPreferences

        binding.viewFab.setOnClickListener {
            binding.viewFab.isClickable = false
            val intent = Intent(context, CreateOrEditNoteActivity::class.java)
            startActivity(intent)
        }

        binding.innerFab.setOnClickListener {
            binding.innerFab.isClickable = false
            val intent = Intent(context, CreateOrEditNoteActivity::class.java)
            startActivity(intent)
        }

        recyclerViewDisplay()

        swipeToDelete(binding.rvNote)

        //implementing search
        binding.search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.noteData.visibility = View.GONE
            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (s.toString().isNotEmpty()) {
                    val text = s.toString()
                    val query = "%$text%"
                    if (query.isNotEmpty()) {
                        noteViewModel.searchNote(query).observe(viewLifecycleOwner) {
                            rvNotesAdapter.submitList(it)
                        }
                    } else {
                        observerDataChanges()
                    }
                } else {
                    observerDataChanges()
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })

//        binding.search.setOnFocusChangeListener { _, hasFocus ->
//            if (hasFocus) {
//                (requireActivity() as MainActivity).binding.bottomNavigationView.isVisible = !hasFocus
//            }
//        }

        binding.search.setOnEditorActionListener { v, actionID, _ ->
            if (actionID == EditorInfo.IME_ACTION_SEARCH) {
                v.clearFocus()
            }
            return@setOnEditorActionListener true
        }

        binding.rvNote.setOnTouchListener { _, _ ->
            requireView().hideKeyboard()
            binding.search.clearFocus()
//            (requireActivity() as MainActivity).binding.bottomNavigationView.visibility = View.VISIBLE
            return@setOnTouchListener false
        }

        binding.popUpMenuSort.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetTheme)
            val sheetView = LayoutInflater.from(activity).inflate(R.layout.modal_bottom_sheet_sort, null)

            val newest = sheetView.findViewById<LinearLayout>(R.id.newestSort)
            val oldest = sheetView.findViewById<LinearLayout>(R.id.oldestSort)
            val color = sheetView.findViewById<LinearLayout>(R.id.colorSort)

            val editorSort = sharedPreferencesSort.edit()

            when(sharedPreferencesSort.getString("sort", "0")) {
                "oldest" -> {
                    oldest.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.add_note_bg))
                    newest.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                    color.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                }

                "newest" -> {
                    oldest.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                    newest.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.add_note_bg))
                    color.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                }

                "color" -> {
                    oldest.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                    newest.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                    color.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.add_note_bg))
                }

                else -> {
                    oldest.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.add_note_bg))
                    newest.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                    color.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                }
            }

            newest.setOnClickListener {

                editorSort.putString("sort", "newest")
                editorSort.apply()

                recyclerViewDisplay()

                bottomSheetDialog.dismiss()
            }

            oldest.setOnClickListener {

                editorSort.putString("sort", "oldest")
                editorSort.apply()

                recyclerViewDisplay()

                bottomSheetDialog.dismiss()
            }

            color.setOnClickListener {

                editorSort.putString("sort", "color")
                editorSort.apply()

                recyclerViewDisplay()

                bottomSheetDialog.dismiss()
            }

            bottomSheetDialog.setContentView(sheetView)
            bottomSheetDialog.show()
        }

        binding.popUpMenu.setOnClickListener {
            showMenu()
        }

        binding.rvNote.setOnScrollChangeListener { _, scrollX, scrollY, _, oldScrollY ->

//            if ((activity as MainActivity).binding.bottomNavigationView.isVisible) {
//                binding.rvNote.setPadding(0, 0, 0, 5)
//            } else {
//                binding.rvNote.setPadding(0, 0, 0, 80)
//            }

            when {
                scrollY > oldScrollY -> {
//                    (activity as MainActivity).binding.bottomNavigationView.visibility = View.GONE
                    binding.addNoteFab.visibility = View.GONE
                    binding.innerFab.isClickable = true
                }

                scrollX == scrollY -> {
//                    (activity as MainActivity).binding.bottomNavigationView.visibility = View.VISIBLE
                    binding.addNoteFab.visibility = View.VISIBLE
                    binding.innerFab.isClickable = false
                }
                else -> {
//                    (activity as MainActivity).binding.bottomNavigationView.visibility = View.VISIBLE
                    binding.addNoteFab.visibility = View.VISIBLE
                    binding.innerFab.isClickable = false
                }
            }

        }

        return binding.root
    }

    private fun showMenu() {
        val popupMenu = PopupMenu(requireContext(), binding.popUpMenu)
        val inflater: MenuInflater = popupMenu.menuInflater
        inflater.inflate(R.menu.folder_view_items, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId){
                R.id.createFolder -> {
                    showPopupWindow(requireActivity())
                }
                R.id.view -> {
                    setNotesView()
                }
            }
            true
        }
        binding.popUpMenu.setOnClickListener {
            try {
                val popUp = PopupMenu::class.java.getDeclaredField("mPopup")
                popUp.isAccessible = true
                val menu = popUp.get(popupMenu)
                menu.javaClass
                    .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                    .invoke(menu, true)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                popupMenu.show()
            }
        }
    }

    private fun setNotesView() {
        val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetTheme)
        val sheetView = LayoutInflater.from(activity).inflate(R.layout.modal_bottom_sheet, null)

        val editorView = sharedPreferencesView.edit()

        val listLayout = sheetView.findViewById<LinearLayout>(R.id.list)
        val gridLayout = sheetView.findViewById<LinearLayout>(R.id.grid)

        when(sharedPreferencesView.getString("view", "0")) {
            "list" -> {
                listLayout.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.add_note_bg))
                gridLayout.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
            "grid" -> {
                gridLayout.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.add_note_bg))
                listLayout.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
            else -> {
                gridLayout.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.add_note_bg))
                listLayout.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
        }

        listLayout.setOnClickListener {
            editorView?.putString("view", "list")
            editorView?.apply()
            recyclerViewDisplay()

            bottomSheetDialog.dismiss()
        }

        gridLayout.setOnClickListener {
            editorView?.putString("view", "grid")
            editorView?.apply()
            recyclerViewDisplay()

            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setContentView(sheetView)
        bottomSheetDialog.show()
    }

    private fun showPopupWindow(activity: Activity) {

        val inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.popup_create_folder, null)

        val popupWindow = PopupWindow(
            view,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        // If you want to dismiss the popup window when clicking outside it
        popupWindow.isOutsideTouchable = true
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Show the popup window at the center of the screen
        popupWindow.showAtLocation(activity.findViewById(android.R.id.content), Gravity.CENTER, 0, 0)

        // Dismiss the popup window when the Dismiss button is clicked
        val cancelBtn = view.findViewById<Button>(R.id.cancelBtnFolder)
        cancelBtn.setOnClickListener {
            popupWindow.dismiss()
        }

        val okBtn = view.findViewById<Button>(R.id.addBtn)
        okBtn.setOnClickListener {
            val folderName = view.findViewById<TextInputEditText>(R.id.folderName).text.toString()
            if (folderName.isNotBlank()) {
                noteViewModel.insertFolder(Folder(folderName, -1))
                popupWindow.dismiss()
            }
        }
    }

    private fun swipeToDelete(rvNote: RecyclerView) {
        val swipeToDeleteCallback = object : SwipeToDelete() {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.absoluteAdapterPosition
                val note = rvNotesAdapter.currentList[position]
                var actionBtnTapped = false
                noteViewModel.deleteNote(note)
                binding.search.clearFocus()
                if (binding.search.text.toString().isEmpty()) {
                    observerDataChanges()
                }
                val snackBar = Snackbar.make(requireView(), "Note Deleted", Snackbar.LENGTH_LONG).addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        super.onDismissed(transientBottomBar, event)
                    }

                    override fun onShown(transientBottomBar: Snackbar?) {

                        transientBottomBar?.setAction("UNDO") {
                            noteViewModel.insertNote(note)
                            actionBtnTapped = true
                            binding.noteData.visibility = View.GONE
                        }

                        super.onShown(transientBottomBar)
                    }
                }).apply {
                    animationMode = Snackbar.ANIMATION_MODE_FADE
                    setAnchorView(R.id.innerFab)
                }
                snackBar.setActionTextColor(ContextCompat.getColor(requireContext(), R.color.yellowOrange))
                snackBar.show()
            }

        }

        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(rvNote)

    }

    private fun recyclerViewDisplay() {
        Toast.makeText(requireContext(), "sg", Toast.LENGTH_SHORT).show()
        when (resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                setUpRecyclerView(2)
                setUpFolderRecyclerView(3)
            }
            Configuration.ORIENTATION_LANDSCAPE -> {
                setUpRecyclerView(3)
                setUpFolderRecyclerView(6)
            }
        }
    }

    private fun setUpFolderRecyclerView(spanCount: Int) {
        binding.rvFolder.apply {
            layoutManager = StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL)
//            setHasFixedSize(true)
            rvFoldersAdapter = RVFoldersAdapter()
            rvNotesAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
            adapter = rvFoldersAdapter
        }
        observerFolderDataChanges()
    }

    private fun observerFolderDataChanges() {
//        rvFoldersAdapter.submitList(listOf(Folder("g", -1)))
        noteViewModel.getAllFolders("-null").observe(viewLifecycleOwner) {list->
            if (binding.noteData.isVisible) binding.noData.visibility = View.GONE
            rvFoldersAdapter.submitList(list)
        }
    }

    private fun setUpRecyclerView(spanCount: Int) {
        binding.rvNote.apply {
            val whichView = sharedPreferencesView.getString("view", "0")
            layoutManager = when (whichView) {
                "list" -> {
                    LinearLayoutManager(requireContext())
                }

                "grid" -> {
                    StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL)
                }

                else -> {
                    StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL)
                }
            }
//            setHasFixedSize(true)
            rvNotesAdapter = RVNotesAdapter()
            rvNotesAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
            adapter = rvNotesAdapter
            postponeEnterTransition(300L, TimeUnit.MILLISECONDS)
            viewTreeObserver.addOnPreDrawListener {
                startPostponedEnterTransition()
                true
            }
        }
        observerDataChanges()
    }

    fun observerDataChanges() {

        when(sharedPreferencesSort.getString("sort", "0")) {
            "oldest" -> {
                noteViewModel.getAllNotesByOldest().observe(viewLifecycleOwner) {list->
                    binding.noteData.isVisible = list.isEmpty()
                    rvNotesAdapter.submitList(list)
                }
            }

            "newest" -> {
                noteViewModel.getAllNotesByNewest().observe(viewLifecycleOwner) {list->
                    binding.noteData.isVisible = list.isEmpty()
                    rvNotesAdapter.submitList(list)
                }
            }

            "color" -> {
                noteViewModel.getAllNotesByColor().observe(viewLifecycleOwner) {list->
                    binding.noteData.isVisible = list.isEmpty()
                    rvNotesAdapter.submitList(list)
                }
            }

            else -> {
                noteViewModel.getAllNotesByOldest().observe(viewLifecycleOwner) {list->
                    binding.noteData.isVisible = list.isEmpty()
                    rvNotesAdapter.submitList(list)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        recyclerViewDisplay()
        binding.viewFab.isClickable = true
    }
}