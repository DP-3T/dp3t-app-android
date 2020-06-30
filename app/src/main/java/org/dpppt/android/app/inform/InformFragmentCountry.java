/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package org.dpppt.android.app.inform;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.hbb20.CountryCodePicker;
import org.dpppt.android.app.R;

import java.util.ArrayList;

public class InformFragmentCountry extends Fragment {

	private Button buttonDone;
	private ListView listView;
	private CountryCodePicker ccp;
	private ArrayAdapter<String> adapter;
	private ArrayList<String> countryList;
	private ArrayList<String> countryCodeList;

	public static InformFragmentCountry newInstance() {
		return new InformFragmentCountry();
	}

	public InformFragmentCountry() {
		super(R.layout.fragment_country);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		countryList = new ArrayList<>();
		countryCodeList = new ArrayList<>();

		countryList.add("Cyprus");
		countryCodeList.add("CY");
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		((InformActivity) requireActivity()).allowBackButton(true);
		buttonDone = view.findViewById(R.id.buttonDone);
		listView = view.findViewById(R.id.listView);
		ccp = view.findViewById(R.id.ccp);

		adapter = new ArrayAdapter<>(view.getContext(),
				android.R.layout.simple_list_item_1, countryList);

		updateList();

		//TODO: Populate list automatically
		ccp.setCountryPreference("GR,AT");

		buttonDone.setOnClickListener(v -> {
			Bundle bundle = new Bundle();
			bundle.putStringArrayList("countryCodes", countryCodeList);

			InformFragment informFragment = InformFragment.newInstance();
			informFragment.setArguments(bundle);

			getParentFragmentManager()
					.beginTransaction()
					.setCustomAnimations(R.anim.slide_enter, R.anim.slide_exit, R.anim.slide_pop_enter, R.anim.slide_pop_exit)
					.replace(R.id.inform_fragment_container, informFragment)
					.addToBackStack(InformFragmentCountry.class.getCanonicalName())
					.commit();
		});

		listView.setOnItemClickListener((parent, v, position, id) -> {
			if (position != 0) {
				Toast.makeText(v.getContext(),
						R.string.long_press_to_delete, Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(v.getContext(), R.string.home_country_cannot_be_deleted, Toast.LENGTH_SHORT).show();
			}
		});

		listView.setOnItemLongClickListener((parent, v, position, id) -> {
			if (position == 0) {
				Toast.makeText(v.getContext(), R.string.home_country_cannot_be_deleted, Toast.LENGTH_SHORT).show();
				return true;
			}

			new AlertDialog.Builder(v.getContext()).
					setIcon(android.R.drawable.ic_delete)
					.setTitle("Delete " + countryList.get(position) + " (" + countryCodeList.get(position) + ") ?")
					.setPositiveButton("Yes", (dialog, which) -> {
						countryList.remove(position);
						countryCodeList.remove(position);

						updateList();
					})
					.setNegativeButton("No", null).show();
			return true;
		});

		ccp.setOnCountryChangeListener(() -> {
			if (!countryList.contains(ccp.getSelectedCountryName())) {
				countryList.add(ccp.getSelectedCountryName());
				countryCodeList.add(ccp.getSelectedCountryNameCode());

				updateList();
			}
		});

		view.findViewById(R.id.cancel_button).setOnClickListener(v -> getActivity().finish());
	}

	private void updateList() {
		buttonDone.setEnabled(!adapter.isEmpty());

		int totalHeight = 0;
		for (int i = 0; i < adapter.getCount(); i++) {
			totalHeight += 175;
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight;
		listView.setLayoutParams(params);

		adapter.notifyDataSetChanged();
		listView.setAdapter(adapter);
	}

	@Override
	public void onResume() {
		super.onResume();
	}

}
